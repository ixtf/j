package aeron

import io.aeron.Aeron
import io.aeron.FragmentAssembler
import io.aeron.driver.MediaDriver
import org.agrona.BufferUtil
import org.agrona.concurrent.BusySpinIdleStrategy
import org.agrona.concurrent.UnsafeBuffer

fun main() {
  val driver = MediaDriver.launchEmbedded()
  println("Aeron 目录: ${driver.aeronDirectoryName()}")

  val ctx = Aeron.Context().aeronDirectoryName(driver.aeronDirectoryName())
  val aeron = Aeron.connect(ctx)

  val channel = "aeron:ipc"
  val streamId = 123

  val subscription = aeron.addSubscription(channel, streamId)
  val fragmentHandler = FragmentAssembler { buffer, offset, length, _ ->
    val received = buffer.getStringWithoutLengthUtf8(offset, length)
    println("【订阅者】收到消息: $received")
  }

  val publication = aeron.addPublication(channel, streamId)
  while (!publication.isConnected) {
    Thread.yield() // 这里的等待是必须的，否则 offer 会失败
  }

  val message = "Hello Aeron Zero Copy!"
  val buffer = UnsafeBuffer(BufferUtil.allocateDirectAligned(256, 64))
  val length = buffer.putStringWithoutLengthUtf8(0, message)
  val position = publication.offer(buffer, 0, length)
  if (position > 0) {
    println("【发布者】发送成功，位置: $position")
  }

  val idleStrategy = BusySpinIdleStrategy()
  var receivedCount = 0
  while (receivedCount < 1) {
    val fragmentsRead = subscription.poll(fragmentHandler, 10)
    receivedCount += fragmentsRead
    idleStrategy.idle() // 无消息时自旋
  }
}
