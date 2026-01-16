package com.github.ixtf.broker.verticle

import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import io.netty.util.internal.PlatformDependent

object RSocketMonitorVerticle : BaseCoroutineVerticle() {
  private var checkDirectMemoryId: Long = -1

  override suspend fun start() {
    super.start()
    // 只有开启了 ZERO_COPY 才需要严密监控
    log.info("Starting Direct Memory Monitor...")
    // 每 30 秒执行一次，频率不宜过高
    checkDirectMemoryId = vertx.setPeriodic(30000) { checkDirectMemory() }
  }

  override suspend fun stop() {
    if (checkDirectMemoryId != -1L) vertx.cancelTimer(checkDirectMemoryId)
    super.stop()
  }

  /**
   * 终极调试手段：Netty 泄漏检测器
   *
   * -Dio.netty.leakDetection.level=ADVANCED
   */
  private fun checkDirectMemory() {
    // 获取 Netty 当前使用的堆外内存估值（单位：字节）
    // 注意：这取决于 PlatformDependent 的具体实现版本
    val usedDirectMemory = PlatformDependent.usedDirectMemory()
    val maxDirectMemory = PlatformDependent.maxDirectMemory()

    val usedMb = usedDirectMemory / 1024 / 1024
    val maxMb = maxDirectMemory / 1024 / 1024
    val usagePercentage = (usedDirectMemory.toDouble() / maxDirectMemory.toDouble() * 100).toInt()

    if (usagePercentage > 80) {
      log.error("CRITICAL: Direct Memory usage is high: $usedMb MB / $maxMb MB ($usagePercentage%)")
      // 这里可以触发报警逻辑
    } else {
      log.info("Direct Memory Monitor: $usedMb MB / $maxMb MB ($usagePercentage%)")
    }
  }
}
