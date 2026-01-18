package com.github.ixtf.cqrs.internal.workflow

import cn.hutool.log.Log
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.ixtf.cqrs.client.*
import com.github.ixtf.cqrs.client.ComponentKey
import com.github.ixtf.cqrs.internal.ComponentMethodRefImpl
import com.github.ixtf.cqrs.internal.RefKit
import com.github.ixtf.cqrs.internal.codegen.WorkflowFactory
import com.github.ixtf.cqrs.workflow.Workflow
import io.vertx.core.Future
import io.vertx.core.ThreadingModel.VIRTUAL_THREAD
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deploymentOptionsOf
import java.util.concurrent.TimeUnit
import kotlin.reflect.*

internal data class WorkflowKey<T : Workflow<*>>(val clazz: Class<T>, val entityId: String) :
  ComponentKey<T> {
  override suspend fun invalidate() = CACHE.invalidate(this)
}

private class WorkflowWrapper(val entity: Workflow<*>, val deployFuture: Future<String>) {
  val instanceFuture: Future<Workflow<*>> = deployFuture.map { entity }

  fun undeploy() {
    deployFuture.flatMap { entity.vertx.undeploy(it) }.onFailure { Log.get().error(it) }
  }
}

private val CACHE =
  Caffeine.newBuilder()
    .maximumSize(100_000)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .removalListener<WorkflowKey<*>, WorkflowWrapper> { _, v, _ -> v?.undeploy() }
    .build<WorkflowKey<*>, WorkflowWrapper>()

internal class WorkflowClientImpl(
  private val vertx: Vertx,
  private val factoryMap: Map<Class<out Workflow<*>>, WorkflowFactory<*>>,
  private val workflowId: String,
  private val selfRegion: String? = null,
) : WorkflowClient {
  private inline fun <reified T> createRef(methodRef: KFunction<*>): T {
    val factory = RefKit.lookupFactory(factoryMap, methodRef)
    val wrapper =
      CACHE.get(WorkflowKey(factory.componentClass, workflowId)) { key ->
        val context = WorkflowContextImpl(workflowId, selfRegion)
        val entity = factory.create(context)
        val options = deploymentOptionsOf(threadingModel = VIRTUAL_THREAD)
        val deployFuture = vertx.deployVerticle(entity, options).onFailure { CACHE.invalidate(key) }
        WorkflowWrapper(entity = entity, deployFuture = deployFuture)
      }
    return ComponentMethodRefImpl(wrapper.instanceFuture, methodRef) as T
  }

  override fun <T : Workflow<*>, R> method(methodRef: KSuspendFunction1<T, R>) =
    createRef(methodRef) as ComponentMethodRef<R>

  override fun <T : Workflow<*>, A1, R> method(methodRef: KSuspendFunction2<T, A1, R>) =
    createRef(methodRef) as ComponentMethodRef1<A1, R>

  override fun <T : Workflow<*>, A1, A2, R> method(methodRef: KSuspendFunction3<T, A1, A2, R>) =
    createRef(methodRef) as ComponentMethodRef2<A1, A2, R>

  override fun <T : Workflow<*>, A1, A2, A3, R> method(
    methodRef: KSuspendFunction4<T, A1, A2, A3, R>
  ) = createRef(methodRef) as ComponentMethodRef3<A1, A2, A3, R>

  override fun <T : Workflow<*>, A1, A2, A3, A4, R> method(
    methodRef: KSuspendFunction5<T, A1, A2, A3, A4, R>
  ) = createRef(methodRef) as ComponentMethodRef4<A1, A2, A3, A4, R>

  override fun <T : Workflow<*>, A1, A2, A3, A4, A5, R> method(
    methodRef: KSuspendFunction6<T, A1, A2, A3, A4, A5, R>
  ) = createRef(methodRef) as ComponentMethodRef5<A1, A2, A3, A4, A5, R>
}
