package test

import cn.hutool.crypto.digest.DigestUtil

fun main() {
  val router = ConsistentHashRouter()
  val machineA = Machine("192.168.1.10", 2551)
  val machineB = Machine("192.168.1.11", 2552)
  val machineC = Machine("192.168.1.12", 2553)

  router.addMachine(machineA)
  router.addMachine(machineB)
  router.addMachine(machineC)

  val actorIds = listOf("actor-1", "actor-2", "actor-3", "user-12345", "order-67890")
  actorIds.forEach { id ->
    val targetMachine = router.routeTo(id)
    println("Actor '$id' is routed to machine: ${targetMachine?.id}")
  }
}

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
      val hash = DigestUtil.sha256(virtualNodeKey).hashCode().toLong()
      // val hash = Hash(SHA256).hash(virtualNodeKey.toByteArray()).long
      circle[hash] = machine
    }
  }

  fun removeMachine(machine: Machine) {
    // 移除该机器的所有虚拟节点
    circle.entries.removeAll { it.value == machine }
  }

  fun routeTo(actorId: String): Machine? {
    if (circle.isEmpty()) return null

    val hash = DigestUtil.sha256(actorId).hashCode().toLong()
    // val hash = Hash(SHA256).hash(actorId.toByteArray()).long
    // 找到环上第一个大于等于该哈希值的节点
    val tailMap = circle.tailMap(hash)
    val targetHash = if (tailMap.isEmpty()) circle.firstKey() else tailMap.firstKey()
    return circle[targetHash]
  }
}
