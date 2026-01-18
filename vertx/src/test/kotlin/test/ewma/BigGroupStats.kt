package test.ewma

import kotlin.math.exp
import kotlin.math.ln

data class BigGroupStats(
  val ewmaLogExecTime: Double = 0.0,
  val completedInWindow: Int = 0,
  val totalExecTimeInWindow: Long = 0,
  val windowStart: Long = System.currentTimeMillis(),
  val inflight: Int = 0,
  val alpha: Double = 0.2,
  val windowSize: Int = 20,
) {
  fun score(minScore: Double = 0.05): Double {
    if (completedInWindow == 0) return minScore
    val penalty = 1.0 / (1 + inflight)
    val efficiency = completedInWindow.toDouble() / (totalExecTimeInWindow + 1)
    val latencyFactor = exp(ewmaLogExecTime)
    val raw = efficiency * penalty / latencyFactor
    return maxOf(raw, minScore)
  }

  fun onTaskFinished(execTimeMs: Long, now: Long = System.currentTimeMillis()): BigGroupStats {
    val logSample = ln(execTimeMs.toDouble() + 1)
    val newEwma =
      if (ewmaLogExecTime == 0.0) logSample else alpha * logSample + (1 - alpha) * ewmaLogExecTime
    val newCompleted = completedInWindow + 1
    val newTotalTime = totalExecTimeInWindow + execTimeMs
    // 简化：超出 windowSize 就衰减
    val decay = if (newCompleted > windowSize) 0.5 else 1.0
    return copy(
      ewmaLogExecTime = newEwma,
      completedInWindow = (newCompleted * decay).toInt(),
      totalExecTimeInWindow = (newTotalTime * decay).toLong(),
      inflight = inflight - 1,
    )
  }
}
