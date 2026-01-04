package com.github.ixtf.cqrs.internal

import com.github.ixtf.cqrs.Metadata
import com.github.ixtf.cqrs.client.*
import io.vertx.core.Future
import io.vertx.kotlin.coroutines.coAwait
import java.util.concurrent.CompletionStage
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlinx.coroutines.reactor.mono

internal class ComponentMethodRefImpl(
  private val instanceFuture: Future<*>,
  private val methodRef: KFunction<*>,
) :
  ComponentMethodRef<Any?>,
  ComponentMethodRef1<Any?, Any?>,
  ComponentMethodRef2<Any?, Any?, Any?>,
  ComponentMethodRef3<Any?, Any?, Any?, Any?>,
  ComponentMethodRef4<Any?, Any?, Any?, Any?, Any?>,
  ComponentMethodRef5<Any?, Any?, Any?, Any?, Any?, Any?> {
  override fun withMetadata(metadata: Metadata) = this

  override fun invokeAsync(): CompletionStage<Any?> =
    mono {
        val instance = instanceFuture.coAwait()
        methodRef.callSuspend(instance)
      }
      .toFuture()

  override fun invokeAsync(a1: Any?): CompletionStage<Any?> =
    mono {
        val instance = instanceFuture.coAwait()
        //       RefKit.lookupInvoker(instance).invoke(methodRef.name, arrayOf(a1))
        methodRef.callSuspend(instance, a1)
      }
      .toFuture()

  override fun invokeAsync(a1: Any?, a2: Any?): CompletionStage<Any?> =
    mono {
        val instance = instanceFuture.coAwait()
        // RefKit.lookupInvoker(instance).invoke(methodRef.name, arrayOf(a1, a2))
        methodRef.callSuspend(instance, a1, a2)
      }
      .toFuture()

  override fun invokeAsync(a1: Any?, a2: Any?, a3: Any?): CompletionStage<Any?> =
    mono {
        val instance = instanceFuture.coAwait()
        methodRef.callSuspend(instance, a1, a2, a3)
      }
      .toFuture()

  override fun invokeAsync(a1: Any?, a2: Any?, a3: Any?, a4: Any?): CompletionStage<Any?> =
    mono {
        val instance = instanceFuture.coAwait()
        methodRef.callSuspend(instance, a1, a2, a3, a4)
      }
      .toFuture()

  override fun invokeAsync(
    a1: Any?,
    a2: Any?,
    a3: Any?,
    a4: Any?,
    a5: Any?,
  ): CompletionStage<Any?> =
    mono {
        val instance = instanceFuture.coAwait()
        methodRef.callSuspend(instance, a1, a2, a3, a4, a5)
      }
      .toFuture()
}
