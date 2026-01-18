package test.alg

import com.google.common.collect.Queues

data class AlgTask(val type: AlgTaskType, val id: String) {
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
