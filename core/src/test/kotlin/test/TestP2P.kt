package test

import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestP2P(), deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)).coAwait()

  println("success")
}

private class TestP2P : CoroutineVerticle() {
  private val httpServer by lazy { vertx.createHttpServer() }

  override suspend fun start() {
    super.start()
    val router = Router.router(vertx)
    router.route().handler { rc -> rc.response().send("p2p: ${rc.request().remoteAddress()}") }
    httpServer.requestHandler(router).listen(12345).coAwait()
  }
}
