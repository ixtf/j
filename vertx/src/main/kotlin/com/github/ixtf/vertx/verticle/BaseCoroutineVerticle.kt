package com.github.ixtf.vertx.verticle

import cn.hutool.log.Log
import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.CoroutineEventBusSupport
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import io.vertx.kotlin.coroutines.CoroutineVerticle

abstract class BaseCoroutineVerticle :
  CoroutineVerticle(), CoroutineEventBusSupport, CoroutineRouterSupport {
  protected val log: Log by lazy { Log.get(this::class.java) }
  protected val eventBus: EventBus by lazy { vertx.eventBus() }
  protected val fileSystem: FileSystem by lazy { vertx.fileSystem() }
  private lateinit var context: Context

  override fun init(vertx: Vertx, context: Context) {
    super.init(vertx, context)
    this.context = context
  }

  protected fun runOnContext(action: Handler<Void>) = context.runOnContext(action)
}
