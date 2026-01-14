package broker

import com.github.ixtf.broker.BrokerClient
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestBrokerService()).coAwait()

  println("success")
}

private class TestBrokerService : BaseCoroutineVerticle() {
  private val brokerClient = BrokerClient("test", "test")

  override suspend fun start() {
    super.start()
  }
}
