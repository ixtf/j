package com.github.ixtf.core.kit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

fun <T, R> Iterable<T>.parallelProcess(block: suspend (T) -> R): Flow<R> = channelFlow {
  forEach { launch { send(block(it)) } }
}

fun <T, R> Sequence<T>.parallelProcess(block: suspend (T) -> R): Flow<R> = channelFlow {
  forEach { launch { send(block(it)) } }
}

fun <T, R> Flow<T>.parallelProcess(block: suspend (T) -> R): Flow<R> = channelFlow {
  collect { launch { send(block(it)) } }
}

suspend fun <T, R> Iterable<T>.parallelCollect(block: suspend (T) -> R) =
  parallelProcess { block(it) }.collect()

suspend fun <T, R> Iterable<T>.parallelToList(block: suspend (T) -> R): List<R> =
  parallelProcess { block(it) }.toList()

suspend fun <T, R> Sequence<T>.parallelCollect(block: suspend (T) -> R) =
  parallelProcess { block(it) }.collect()

suspend fun <T, R> Sequence<T>.parallelToList(block: suspend (T) -> R): List<R> =
  parallelProcess { block(it) }.toList()

suspend fun <T, R> Flow<T>.parallelCollect(block: suspend (T) -> R) =
  parallelProcess { block(it) }.collect()

suspend fun <T, R> Flow<T>.parallelToList(block: suspend (T) -> R): List<R> =
  parallelProcess { block(it) }.toList()
