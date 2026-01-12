package com.github.ixtf.vertx.verticle

import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.receiveChannelHandler
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import reactor.util.function.Tuple3
import reactor.util.function.Tuples

abstract class ActorVerticle<S, E> : BaseCoroutineVerticle() {
  private val channel by lazy { vertx.receiveChannelHandler<Pair<Promise<S>, List<E>>>() }
  private var _currentState: S? = null

  suspend fun currentState(): S = requireNotNull(_currentState)

  protected abstract suspend fun emptyState(): S?

  protected abstract suspend fun persist(entity: S)

  protected abstract suspend fun applyEvent(event: E): S

  protected open suspend fun handleEffect(envelopes: Collection<Tuple3<S, E, S>>) = Unit

  override suspend fun start() {
    super.start()
    _currentState = emptyState()
    launch {
      channel.consumeEach { (promise, events) ->
        val before = _currentState
        try {
          val effects = handleEvent(events)
          persist(currentState())
          launch { handleEffect(effects) }
          promise.complete(currentState())
        } catch (_: CancellationException) {
          // ignore
        } catch (t: Throwable) {
          _currentState = before
          promise.fail(t)
          log.error(t, "state: {}", before)
        }
      }
    }
  }

  protected open suspend fun handleEvent(events: List<E>) =
    events.map { event ->
      val previous = _currentState
      _currentState = applyEvent(event)
      Tuples.of(previous, event, currentState())
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
}
