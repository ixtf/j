package test

import cn.hutool.core.util.RandomUtil
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import com.google.common.collect.Queues
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val vertx = Vertx.vertx()

suspend fun main() {
  vertx.deployVerticle(TestVerticle()).coAwait()
}

enum class AlgTaskType {
  RUN_ALG,
  RUN_REPORT,
  RUN_MARKET,
  RUN_JOB,
}

private data class AlgTask(val type: AlgTaskType, val id: String) {
  companion object {
    private val fakeMQ = buildMap {
      AlgTaskType.entries.forEachIndexed { index, type ->
        val taskQueue = Queues.newConcurrentLinkedQueue<AlgTask>()
        if (index % 2 == 0) (1..5).forEach { idx -> taskQueue.add(AlgTask(type, "task-$idx")) }
        put(type, taskQueue)
      }
    }

    fun fetchTask(type: AlgTaskType) = fakeMQ[type]?.poll()
  }
}

private class TestVerticle : BaseCoroutineVerticle() {
  private val preparedSlot = Channel<AlgTask>()

  private val queues = AlgTaskType.entries.map { it }.sorted()
  private var lastQueue: AlgTaskType? = null

  @OptIn(ExperimentalCoroutinesApi::class)
  override suspend fun start() {
    super.start()
    launch {
      while (isActive) {
        val task = fetchTask()
        val prepared = task.prepare()
        preparedSlot.send(prepared)
      }
    }
    launch {
      preparedSlot.consumeEach { task -> runCatching { task.invoke() }.onFailure { log.error(it) } }
    }
  }

  private suspend fun AlgTask.invoke() = apply {
    log.info("invoke[$type]: $id")
    when (type) {
      AlgTaskType.RUN_ALG -> delay(RandomUtil.randomLong(3000, 5000))
      AlgTaskType.RUN_REPORT -> delay(RandomUtil.randomLong(3000, 5000))
      AlgTaskType.RUN_MARKET -> delay(RandomUtil.randomLong(3000, 5000))
      AlgTaskType.RUN_JOB -> delay(RandomUtil.randomLong(3000, 5000))
    }
  }

  private suspend fun AlgTask.prepare() = apply {
    log.info("prepare[$type]: $id")
    when (type) {
      AlgTaskType.RUN_ALG -> delay(RandomUtil.randomLong(1000, 3000))
      AlgTaskType.RUN_REPORT -> delay(RandomUtil.randomLong(1000, 3000))
      AlgTaskType.RUN_MARKET -> delay(RandomUtil.randomLong(1000, 3000))
      AlgTaskType.RUN_JOB -> delay(RandomUtil.randomLong(1000, 3000))
    }
  }

  private suspend fun fetchTask(): AlgTask {
    while (true) {
      val startIdx = (queues.indexOf(lastQueue) + 1) % queues.size
      for (i in queues.indices) {
        val queue = queues[(startIdx + i) % queues.size]
        val task = AlgTask.fetchTask(queue)
        if (task != null) {
          lastQueue = queue
          return task
        }
      }
      delay(1000)
    }
  }
}
