package com.github.ixtf.broker.internal.kit

import com.github.ixtf.broker.kit.toPayload
import io.rsocket.Payload
import io.rsocket.core.Resume
import io.vertx.core.buffer.Buffer
import java.time.Duration
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec

internal object InternalKit {
  internal fun buildConnectionSetupPayload(token: String): Mono<Payload> = mono {
    Buffer.buffer(token).toPayload()
  }

  internal fun defaultRetry(source: Any): RetryBackoffSpec =
    Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
      .maxBackoff(Duration.ofSeconds(3))
      .jitter(0.5)
      .doBeforeRetry { println("$source，尝试第 ${it.totalRetries() + 1} 次重连...") }

  /**
   * - server：需要Resume
   * - service：不要Resume，否则连接断开会延迟到Resume过期
   * - client：可以Resume
   */
  internal fun defaultResume(source: Any): Resume =
    Resume().sessionDuration(Duration.ofMinutes(5)).retry(defaultRetry(source))
}
