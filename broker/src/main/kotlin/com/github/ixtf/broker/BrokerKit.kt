package com.github.ixtf.broker

import io.cloudevents.CloudEvent
import io.rsocket.Payload
import io.rsocket.transport.ClientTransport
import io.rsocket.transport.ServerTransport
import io.rsocket.transport.netty.client.TcpClientTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import io.rsocket.util.DefaultPayload
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

object BrokerKit {

  fun Buffer.toPayload(): Payload = DefaultPayload.create(bytes)

  fun Payload.toBuffer(): Buffer = Buffer.buffer(data().array())

  fun JsonObject.toPayload(): Payload = toBuffer().toPayload()

  fun Payload.toJsonObject(): JsonObject = toBuffer().toJsonObject()

  fun JsonArray.toPayload(): Payload = toBuffer().toPayload()

  fun JsonArray.toJsonArray(): JsonArray = toBuffer().toJsonArray()

  fun CloudEvent.toPayload(): Payload {
    TODO()
  }

  fun Payload.toCloudEvent(): CloudEvent {
    TODO()
  }
}
