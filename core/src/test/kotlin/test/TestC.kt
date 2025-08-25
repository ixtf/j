package test

import kotlinx.serialization.hashing.Hash
import kotlinx.serialization.hashing.SHA256
import kotlinx.serialization.hashing.hashing

class TestC {}

data class Machine(val ip: String, val port: Int) {
  // 机器的唯一标识
  val id = "$ip:$port"
}

class ConsistentHashRouter(private val virtualNodeCount: Int = 100) {
  // 哈希环：存储 (哈希值 -> 机器)
  private val circle = sortedMapOf<Long, Machine>()

  fun addMachine(machine: Machine) {
    // 为每台物理机器添加多个虚拟节点
    for (i in 0 until virtualNodeCount) {
      val virtualNodeKey = "${machine.id}#$i"
      val hash = Hash(SHA256).hash(virtualNodeKey.toByteArray()).long
      circle[hash] = machine
    }
  }

  fun removeMachine(machine: Machine) {
    // 移除该机器的所有虚拟节点
    circle.entries.removeAll { it.value == machine }
  }

  fun routeTo(actorId: String): Machine? {
    if (circle.isEmpty()) return null

    val hash = Hash(SHA256).hash(actorId.toByteArray()).long
    // 找到环上第一个大于等于该哈希值的节点
    val tailMap = circle.tailMap(hash)
    val targetHash = if (tailMap.isEmpty()) circle.firstKey() else tailMap.firstKey()
    return circle[targetHash]
  }
}
