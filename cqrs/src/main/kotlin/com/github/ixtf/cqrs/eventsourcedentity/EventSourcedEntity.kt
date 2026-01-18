package com.github.ixtf.cqrs.eventsourcedentity

import com.github.ixtf.cqrs.client.ComponentKey
import com.github.ixtf.cqrs.consumer.MessageEnvelope
import com.github.ixtf.cqrs.internal.eventsourcedentity.EventSourcedEntityContextImpl.Companion.ebAddress
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.receiveChannelHandler
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

abstract class EventSourcedEntity<S, E>(context: EventSourcedEntityContext) :
  BaseCoroutineVerticle() {
  val entityId by context::entityId
  val componentKey by lazy { ComponentKey.forEventSourcedEntity(this::class.java, entityId) }
  private val ebAddress by lazy { this.ebAddress() }
  private val channel by lazy { vertx.receiveChannelHandler<Pair<Promise<S>, List<E>>>() }
  private var _currentState: S? = null

  suspend fun currentState(): S = requireNotNull(_currentState)

  protected abstract suspend fun emptyState(): S?

  protected abstract suspend fun persist(entity: S)

  protected abstract suspend fun applyEvent(event: E): S

  override suspend fun start() {
    super.start()
    _currentState = emptyState()
    launch {
      channel.consumeEach { (promise, events) ->
        val before = _currentState
        try {
          val envelopes = events.map { handle(it) }
          persist(currentState())
          launch { publishEffect(envelopes) }
          promise.complete(currentState())
        } catch (_: CancellationException) {
          // ignore
        } catch (e: Throwable) {
          _currentState = before
          promise.fail(e)
          log.error(e, "${javaClass.simpleName}: {}", entityId)
        }
      }
    }
  }

  override suspend fun stop() {
    channel.cancel(null)
    // TODO: 保存当前状态
    // persist(currentState())
    super.stop()
  }

  protected suspend fun persist(vararg events: E): S = persist(listOf(*events))

  protected suspend fun persist(events: List<E>): S {
    val promise = Promise.promise<S>()
    channel.handle(promise to events)
    return promise.future().coAwait()
  }

  private suspend fun handle(event: E): MessageEnvelope<S, E> {
    val previous = _currentState
    _currentState = applyEvent(event)
    return MessageEnvelope(previous = previous, event = event, current = currentState())
  }

  private fun publishEffect(envelopes: List<MessageEnvelope<S, E>>) {
    envelopes.forEach { envelope -> eventBus.publish(ebAddress, envelope) }
  }
}
