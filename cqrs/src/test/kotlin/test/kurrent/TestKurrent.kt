package test.kurrent

import com.github.ixtf.core.DefaultOperator
import com.github.ixtf.core.J
import com.github.ixtf.core.JSON_MAPPER
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.kurrent.dbclient.AppendToStreamOptions
import io.kurrent.dbclient.CreatePersistentSubscriptionToStreamOptions
import io.kurrent.dbclient.EventData
import io.kurrent.dbclient.KurrentDBClient
import io.kurrent.dbclient.KurrentDBConnectionString
import io.kurrent.dbclient.KurrentDBPersistentSubscriptionsClient
import io.kurrent.dbclient.NamedConsumerStrategy
import io.kurrent.dbclient.PersistentSubscription
import io.kurrent.dbclient.PersistentSubscriptionListener
import io.kurrent.dbclient.ReadStreamOptions
import io.kurrent.dbclient.ResolvedEvent
import io.kurrent.dbclient.StreamState
import io.vertx.core.Vertx
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.future.await

private val vertx = Vertx.builder().with(vertxOptionsOf(preferNativeTransport = true)).build()
private val client =
  KurrentDBClient.create(
    KurrentDBConnectionString.parseOrThrow(
      "kurrentdb://admin:changeit@git.medipath.com.cn:2113?tls=false"
    )
  )
private val subscriptionsClient = KurrentDBPersistentSubscriptionsClient.from(client)
private const val streamName = "test-123"
private const val CORRELATION_ID = $$"$correlationId"
private const val CAUSATION_ID = $$"$causationId" // 可选：追踪是谁导致了谁

data class TestEvent(
  val name: String,
  val fireDateTime: Instant = Instant.now(),
  val fireOperator: DefaultOperator = DefaultOperator(id = "id", name = "name"),
)

suspend fun main() {
  vertx.deployVerticle(SubscriptionVerticle).coAwait()
  vertx.deployVerticle(AppendVerticle).coAwait()
  vertx.deployVerticle(ReadVerticle).coAwait()
}

private object AppendVerticle : BaseCoroutineVerticle() {
  override suspend fun start() {
    super.start()
    appendToStream()
  }

  private suspend fun appendToStream() {
    val options =
      AppendToStreamOptions.get().streamState(StreamState.noStream()).streamState(StreamState.any())
    val data = JSON_MAPPER.writeValueAsBytes(TestEvent("test"))
    val metadata =
      JSON_MAPPER.writeValueAsBytes(
        mapOf(
          CORRELATION_ID to J.objectId()
          // $$"$causationId" to eventData.eventId.toString(), // 可选：追踪是谁导致了谁
        )
      )
    val eventData = EventData.builderAsJson("test", data).metadataAsBytes(metadata).build()
    val result = client.appendToStream(streamName, options, eventData).await()
    println(result)
  }
}

private object ReadVerticle : BaseCoroutineVerticle() {
  override suspend fun start() {
    super.start()
    readStream()
  }

  private suspend fun readStream() {
    val options = ReadStreamOptions.get()
    val result = client.readStream(streamName, options).await()
    result.events.forEach { println(it) }
  }
}

private object SubscriptionVerticle : BaseCoroutineVerticle() {
  private lateinit var subscription: PersistentSubscription
  private const val STREAM_CATEGORY = $$"$ce-test"
  private const val STREAM_GROUP = "test-projection-group"

  override suspend fun start() {
    super.start()
    ensureSubscriptionGroup()
    subscription = subscribeToStream().await()
  }

  override suspend fun stop() {
    if (::subscription.isInitialized) subscription.stop()
    client.shutdown().await()
    super.stop()
  }

  private suspend fun ensureSubscriptionGroup() {
    try {
      val settings =
        CreatePersistentSubscriptionToStreamOptions.get()
          .namedConsumerStrategy(NamedConsumerStrategy.PINNED)
          .messageTimeout(Duration.ofSeconds(60))
          .resolveLinkTos()
      subscriptionsClient.createToStream(STREAM_CATEGORY, STREAM_GROUP, settings).await()
    } catch (e: Exception) {
      if (!(e is StatusRuntimeException && e.status.code == Status.ALREADY_EXISTS.code)) {
        log.error("Failed to create subscription", e)
        throw e
      }
    }
  }

  private fun subscribeToStream(): CompletableFuture<PersistentSubscription> =
    subscriptionsClient.subscribeToStream(
      STREAM_CATEGORY,
      STREAM_GROUP,
      object : PersistentSubscriptionListener() {
        override fun onEvent(
          subscription: PersistentSubscription,
          retryCount: Int,
          resolvedEvent: ResolvedEvent,
        ) {
          val event = resolvedEvent.event
          val revision = event.revision
          log.info("onEvent: {}, {}, {}", subscription.subscriptionId, retryCount, resolvedEvent)
          subscription.ack(resolvedEvent)
        }

        override fun onCancelled(subscription: PersistentSubscription?, exception: Throwable?) {
          log.error(exception, "onCancelled: {}", subscription?.subscriptionId)
          vertx.setTimer(3000) { subscribeToStream() }
        }
      },
    )
}
