package com.github.ixtf.cqrs.client

import com.github.ixtf.cqrs.Metadata
import java.util.concurrent.CompletionStage
import kotlinx.coroutines.future.await

interface ComponentMethodRef<R> {
  fun withMetadata(metadata: Metadata): ComponentMethodRef<R>

  fun invokeAsync(): CompletionStage<R>

  suspend fun invoke(): R = invokeAsync().await()
}

interface ComponentMethodRef1<A1, R> {
  fun withMetadata(metadata: Metadata): ComponentMethodRef1<A1, R>

  fun invokeAsync(a1: A1): CompletionStage<R>

  suspend fun invoke(a1: A1): R = invokeAsync(a1).await()
}

interface ComponentMethodRef2<A1, A2, R> {
  fun withMetadata(metadata: Metadata): ComponentMethodRef2<A1, A2, R>

  fun invokeAsync(a1: A1, a2: A2): CompletionStage<R>

  suspend fun invoke(a1: A1, a2: A2): R = invokeAsync(a1, a2).await()
}

interface ComponentMethodRef3<A1, A2, A3, R> {
  fun withMetadata(metadata: Metadata): ComponentMethodRef3<A1, A2, A3, R>

  fun invokeAsync(a1: A1, a2: A2, a3: A3): CompletionStage<R>

  suspend fun invoke(a1: A1, a2: A2, a3: A3): R = invokeAsync(a1, a2, a3).await()
}

interface ComponentMethodRef4<A1, A2, A3, A4, R> {
  fun withMetadata(metadata: Metadata): ComponentMethodRef4<A1, A2, A3, A4, R>

  fun invokeAsync(a1: A1, a2: A2, a3: A3, a4: A4): CompletionStage<R>

  suspend fun invoke(a1: A1, a2: A2, a3: A3, a4: A4): R = invokeAsync(a1, a2, a3, a4).await()
}

interface ComponentMethodRef5<A1, A2, A3, A4, A5, R> {
  fun withMetadata(metadata: Metadata): ComponentMethodRef5<A1, A2, A3, A4, A5, R>

  fun invokeAsync(a1: A1, a2: A2, a3: A3, a4: A4, a5: A5): CompletionStage<R>

  suspend fun invoke(a1: A1, a2: A2, a3: A3, a4: A4, a5: A5): R =
    invokeAsync(a1, a2, a3, a4, a5).await()
}
