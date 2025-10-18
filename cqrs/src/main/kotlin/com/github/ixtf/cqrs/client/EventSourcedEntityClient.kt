package com.github.ixtf.cqrs.client

import com.gitee.ixtf.cqrs.verticle.EventSourcedEntity
import kotlin.reflect.*

interface EventSourcedEntityClient {
  fun <T : EventSourcedEntity<*, *>, R> method(
      methodRef: KSuspendFunction1<T, R>
  ): ComponentMethodRef<R>

  fun <T : EventSourcedEntity<*, *>, A1, R> method(
      methodRef: KSuspendFunction2<T, A1, R>
  ): ComponentMethodRef1<A1, R>

  fun <T : EventSourcedEntity<*, *>, A1, A2, R> method(
      methodRef: KSuspendFunction3<T, A1, A2, R>
  ): ComponentMethodRef2<A1, A2, R>

  fun <T : EventSourcedEntity<*, *>, A1, A2, A3, R> method(
      methodRef: KSuspendFunction4<T, A1, A2, A3, R>
  ): ComponentMethodRef3<A1, A2, A3, R>

  fun <T : EventSourcedEntity<*, *>, A1, A2, A3, A4, R> method(
      methodRef: KSuspendFunction5<T, A1, A2, A3, A4, R>
  ): ComponentMethodRef4<A1, A2, A3, A4, R>

  fun <T : EventSourcedEntity<*, *>, A1, A2, A3, A4, A5, R> method(
      methodRef: KSuspendFunction6<T, A1, A2, A3, A4, A5, R>
  ): ComponentMethodRef5<A1, A2, A3, A4, A5, R>
}
