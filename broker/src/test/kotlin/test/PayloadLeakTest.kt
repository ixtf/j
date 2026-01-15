package test

import com.github.ixtf.broker.readValueOrNull
import io.netty.buffer.Unpooled
import io.rsocket.util.DefaultPayload
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PayloadLeakTest {
  @Test
  fun `test readValueOrNull should release payload and decrement refCount`() {
    // 1. 创建一个 Direct Buffer (模拟真实的堆外内存环境)
    val content = """{"name":"test"}"""
    val data = Unpooled.directBuffer().writeBytes(content.toByteArray())

    // 初始引用计数应为 1
    assertEquals(1, data.refCnt(), "初始 ByteBuf 引用计数应为 1")

    // 2. 封装进 Payload
    val payload = DefaultPayload.create(data)
    // 此时 Payload 持有该 ByteBuf，计数依然是 1 (或者增加，取决于实现)
    val initialRefCount = data.refCnt()

    // 3. 执行你的扩展函数进行解析
    // 假设你要解析成 Map
    val result: Map<String, String>? = payload.readValueOrNull<Map<String, String>>()

    // 4. 验证结果
    assertEquals("test", result?.get("name"))

    // 5. 核心验证：验证引用计数
    // 由于你在 Payload.readValueOrNull 中执行了 safeRelease(this)
    // 底层的 ByteBuf 引用计数应该已经减 1
    assertEquals(0, data.refCnt(), "执行解析后，ByteBuf 引用计数必须归零，否则存在内存泄漏")
  }

  @Test
  fun `test readValueOrNull with exception should still release payload`() {
    // 1. 创建损坏的 JSON 数据
    val data = Unpooled.directBuffer().writeBytes("invalid json".toByteArray())
    val payload = DefaultPayload.create(data)

    // 2. 执行解析（预期会失败并触发 finally 块）
    val result = runCatching { payload.readValueOrNull<Map<String, String>>() }.getOrNull()

    // 3. 验证
    assertEquals(null, result, "解析失败应返回 null")
    assertEquals(0, data.refCnt(), "即使解析发生异常，finally 块也必须释放 Payload")
  }
}
