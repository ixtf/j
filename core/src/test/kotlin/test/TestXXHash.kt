package test

import cn.hutool.core.util.HashUtil
import com.github.ixtf.core.kit.base58
import com.github.ixtf.core.kit.base62
import java.nio.ByteBuffer
import java.time.Instant
import net.openhft.hashing.LongHashFunction

private val hashFunc = LongHashFunction.xx3()

private fun ByteArray.generateEfficientId(): ByteArray {
  val buffer = ByteBuffer.allocate(16)
  val epochSeconds = Instant.now().epochSecond.toInt()
  buffer.putInt(epochSeconds)

  val low = LongHashFunction.xx128low().hashBytes(this)
  val high = LongHashFunction.xx3(low).hashBytes(this)
  buffer.putLong(low)
  buffer.putInt((high ushr 32).toInt())
  return buffer.array()
}

private fun ByteArray.generateLongId(epochSecond: Long): Long {
  val hash = LongHashFunction.xx3().hashBytes(this)
  // 保持高位为时间戳 (32位)，低位为哈希 (32位)
  // 但为了降低碰撞，建议 hash 取 xx3 的高 32 位和低 32 位的异或结果，增加散列度
  val mixedHash = (hash ushr 32) xor (hash and 0xFFFFFFFFL)
  return (epochSecond shl 32) or (mixedHash and 0xFFFFFFFFL)
}

fun main() {
  val bytes = "123".toByteArray() + "456".toByteArray()
  println(bytes.generateEfficientId().base58())
  println(bytes.generateEfficientId().base62())
  println(hashFunc.hashBytes(bytes))
  println(hashFunc.hashBytes(bytes))

  repeat(10) {
    println(hashFunc.hashLongs(longArrayOf(hashFunc.hashChars("123"), hashFunc.hashChars("$it"))))
  }

  println(HashUtil.murmur64(bytes))
}
