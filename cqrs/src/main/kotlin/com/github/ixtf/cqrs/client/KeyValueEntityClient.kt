package com.github.ixtf.cqrs.client

import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import kotlin.reflect.KSuspendFunction1
import kotlin.reflect.KSuspendFunction2
import kotlin.reflect.KSuspendFunction3
import kotlin.reflect.KSuspendFunction4
import kotlin.reflect.KSuspendFunction5
import kotlin.reflect.KSuspendFunction6

interface KeyValueEntityClient {
  fun <T : KeyValueEntity<*>, R> method(methodRef: KSuspendFunction1<T, R>): ComponentMethodRef<R>

  fun <T : KeyValueEntity<*>, A1, R> method(
    methodRef: KSuspendFunction2<T, A1, R>
  ): ComponentMethodRef1<A1, R>

  fun <T : KeyValueEntity<*>, A1, A2, R> method(
    methodRef: KSuspendFunction3<T, A1, A2, R>
  ): ComponentMethodRef2<A1, A2, R>

  fun <T : KeyValueEntity<*>, A1, A2, A3, R> method(
    methodRef: KSuspendFunction4<T, A1, A2, A3, R>
  ): ComponentMethodRef3<A1, A2, A3, R>

  fun <T : KeyValueEntity<*>, A1, A2, A3, A4, R> method(
    methodRef: KSuspendFunction5<T, A1, A2, A3, A4, R>
  ): ComponentMethodRef4<A1, A2, A3, A4, R>

  fun <T : KeyValueEntity<*>, A1, A2, A3, A4, A5, R> method(
    methodRef: KSuspendFunction6<T, A1, A2, A3, A4, A5, R>
  ): ComponentMethodRef5<A1, A2, A3, A4, A5, R>
}
