package com.github.ixtf.cqrs.consumer

import com.github.ixtf.cqrs.annotations.Consume
import com.github.ixtf.cqrs.internal.eventsourcedentity.EventSourcedEntityContextImpl.Companion.ebAddress
import com.github.ixtf.cqrs.internal.keyvalueentity.KeyValueEntityContextImpl.Companion.ebAddress
import com.github.ixtf.cqrs.internal.workflow.WorkflowContextImpl.Companion.ebAddress
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.toReceiveChannel
import kotlin.requireNotNull
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

abstract class Consumer : BaseCoroutineVerticle() {
  protected val consumer: MessageConsumer<MessageEnvelope<*, *>> by lazy {
    val address =
      this::class.annotations.firstNotNullOfOrNull { annotation ->
        when (annotation) {
          is Consume.FromEventSourcedEntity -> ebAddress(annotation.value.java)
          is Consume.FromKeyValueEntity -> ebAddress(annotation.value.java)
          is Consume.FromWorkflow -> ebAddress(annotation.value.java)
          else -> null
        }
      }
    requireNotNull(address) {
      "${this::class.java.name}: Consumer must have exactly one supported @Consume annotation."
    }
    eventBus.consumer(address)
  }

  override suspend fun start() {
    super.start()
    vertx
      .sharedData()
      .getLocalCounter(consumer.address())
      .compose { it.incrementAndGet() }
      .coAwait()

    launch {
      consumer.toReceiveChannel(vertx).consumeEach { reply ->
        runCatching { handle(reply.body()) }
          .onFailure { log.error(it, "address: ${consumer.address()} \n body: ${reply.body()}") }
      }
    }
    consumer.completion().coAwait()

    println("deploy[1]: ${javaClass.name}")
  }

  override suspend fun stop() {
    consumer.unregister().coAwait()
    super.stop()
  }

  protected abstract suspend fun handle(message: MessageEnvelope<*, *>)
}
