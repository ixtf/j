package mdns

import java.net.DatagramSocket
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo

private const val SERVICE_TYPE = "_rsocket-cluster._tcp.local."
private val NODE_NAME = "node-${System.currentTimeMillis()}"
private const val PORT = 7000

fun main() {
  val addr =
    DatagramSocket().use {
      it.connect(InetAddress.getByName("8.8.8.8"), 53)
      it.localAddress.also { println(it) }
    }
  val jmdns = JmDNS.create(addr)
  val serviceInfo = ServiceInfo.create(SERVICE_TYPE, NODE_NAME, PORT, "cluster node")
  jmdns.registerService(serviceInfo)
  println("mDNS 已广播服务: $NODE_NAME")

  jmdns.addServiceListener(
    SERVICE_TYPE,
    object : javax.jmdns.ServiceListener {
      override fun serviceAdded(event: ServiceEvent) {
        // 当发现有新节点加入时，请求解析它的 IP 和端口
        jmdns.requestServiceInfo(event.type, event.name)
      }

      override fun serviceRemoved(event: ServiceEvent) {
        println("节点下线: ${event.name}")
      }

      override fun serviceResolved(event: ServiceEvent) {
        // 拿到其他节点的详细信息
        val info = event.info
        val remoteHost = info.inetAddresses[0].hostAddress
        val remotePort = info.port

        if (info.name != NODE_NAME) { // 排除掉自己
          println("发现新节点：${info.name} 地址：$remoteHost:$remotePort")
          // 这里就可以去调用上一条回复中的 RSocketClient 逻辑进行连接了
        }
      }
    },
  )
}
