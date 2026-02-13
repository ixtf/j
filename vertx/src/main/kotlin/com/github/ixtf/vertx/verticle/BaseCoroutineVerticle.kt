package com.github.ixtf.vertx.verticle

import cn.hutool.log.Log
import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.file.FileSystem
import io.vertx.core.internal.ContextInternal
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.CoroutineEventBusSupport
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import io.vertx.kotlin.coroutines.CoroutineVerticle

abstract class BaseCoroutineVerticle :
  CoroutineVerticle(), CoroutineEventBusSupport, CoroutineRouterSupport {
  protected open val log: Log by lazy { Log.get(javaClass) }
  protected open val eventBus: EventBus by lazy { vertx.eventBus() }
  protected open val fileSystem: FileSystem by lazy { vertx.fileSystem() }
  protected open val webClient: WebClient by lazy { WebClient.create(vertx) }
  protected open lateinit var contextInternal: ContextInternal

  override fun init(vertx: Vertx, context: Context) {
    super.init(vertx, context)
    contextInternal = context as ContextInternal
    context.exceptionHandler { log.error(it) }
  }

  protected fun runOnContext(action: Handler<Void>) = contextInternal.runOnContext(action)

  protected fun <E> dispatch(event: E, handler: Handler<E>) =
    contextInternal.dispatch(event, handler)
}
