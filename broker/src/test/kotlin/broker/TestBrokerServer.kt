package broker

import com.github.ixtf.broker.verticle.BrokerServerVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestBrokerServer()).coAwait()
}

private class TestBrokerServer : BrokerServerVerticle() {}
