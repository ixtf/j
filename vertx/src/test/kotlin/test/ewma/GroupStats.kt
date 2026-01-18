package test.ewma

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

data class GroupStats(
  val ewmaLogExecTime: Double = 0.0,
  val completedInWindow: Int = 0,
  val totalExecTimeInWindow: Long = 0,
  val inflight: Int = 0,
  val remainingTasks: Int = Int.MAX_VALUE,
  val alpha: Double = 0.2,
  val windowSize: Int = 20,
) {
  fun score(minScore: Double = 0.01): Double {
    // 冷启动 / 极慢组保护
    if (completedInWindow == 0 && inflight == 0) {
      return minScore
    }

    // 1. 历史效率（稳定，不爆炸）
    val efficiency = completedInWindow.toDouble() / (totalExecTimeInWindow + 1)
    // 2. 延迟因子（log-space EWMA）
    val latencyFactor = exp(ewmaLogExecTime)
    // 3. 并发惩罚
    val penalty = 1.0 / (1 + inflight)
    // 4. Drain 加权：剩余越少，越容易被清空
    val drainBoost =
      if (remainingTasks == Int.MAX_VALUE) 1.0 else 1.0 / sqrt(remainingTasks.toDouble() + 1)
    val raw = efficiency * penalty * drainBoost / latencyFactor
    return max(raw, minScore)
  }

  fun onDispatch(): GroupStats = copy(inflight = inflight + 1)

  fun onTaskFinished(execTimeMs: Long): GroupStats {
    val logSample = ln(execTimeMs.toDouble() + 1)

    val newEwma =
      if (ewmaLogExecTime == 0.0) logSample else alpha * logSample + (1 - alpha) * ewmaLogExecTime

    val newCompleted = completedInWindow + 1
    val newTotalTime = totalExecTimeInWindow + execTimeMs

    val decay = if (newCompleted > windowSize) 0.5 else 1.0

    return copy(
      ewmaLogExecTime = newEwma,
      completedInWindow = (newCompleted * decay).toInt(),
      totalExecTimeInWindow = (newTotalTime * decay).toLong(),
      inflight = inflight - 1,
      remainingTasks =
        if (remainingTasks == Int.MAX_VALUE) remainingTasks else max(remainingTasks - 1, 0),
    )
  }
}
