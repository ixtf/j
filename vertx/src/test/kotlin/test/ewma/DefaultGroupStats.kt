package test.ewma

const val WINDOW_MS = 30_000
// 否则新 group 会被饿死。
const val DEFAULT_NEW_GROUP_SCORE = 30_000
const val MIN_SCORE = 30_000

data class DefaultGroupStats(
  var ewmaExecTime: Double = 0.0,
  var completedInWindow: Int = 0,
  var windowStart: Long = System.currentTimeMillis(),
  var inflight: Int = 0,
  val alpha: Double = 0.3,
  val windowMs: Long = 30_000,
) {
  /**
   * 调度评分
   * 1. 吞吐（近期） throughput = completed_in_window / window_seconds
   * 2. 执行耗时（EWMA）latency = ewma_exec_time
   * 3. 并发惩罚（非常关键）penalty = 1 / (1 + inflight)
   *
   * score = throughput × penalty ÷ (latency + ε)
   */
  fun score(windowSeconds: Double = 30.0, minScore: Double = 0.05): Double {
    val throughput = completedInWindow / windowSeconds
    val penalty = 1.0 / (1 + inflight)
    val latency = ewmaExecTime + 1
    val raw = throughput * penalty / latency
    return maxOf(raw, minScore)
  }

  fun onDispatch(): DefaultGroupStats = copy(inflight = inflight + 1)

  fun onTaskFinished(
    execTimeMs: Long,
    now: Long = System.currentTimeMillis(),
    windowMs: Long = 30_000,
    alpha: Double = 0.3,
  ): DefaultGroupStats {
    val resetWindow = now - windowStart > windowMs
    val newCompleted = if (resetWindow) 1 else completedInWindow + 1
    val newWindowStart = if (resetWindow) now else windowStart

    // 指数加权移动平均
    val newEwma =
      if (ewmaExecTime == 0.0) execTimeMs.toDouble()
      else alpha * execTimeMs + (1 - alpha) * ewmaExecTime

    return copy(
      ewmaExecTime = newEwma,
      completedInWindow = newCompleted,
      windowStart = newWindowStart,
      inflight = inflight - 1,
    )
  }
}

// fun pickGroup(groups: Map<String, GroupStats>): String? {
//  return groups
//    .filter { it.value.inflight < 2 } // 限制慢组并发
//    .maxByOrNull { score(it.value) }
//    ?.key
// }
