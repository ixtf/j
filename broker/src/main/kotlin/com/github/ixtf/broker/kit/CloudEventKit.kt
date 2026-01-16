package com.github.ixtf.broker.kit

import com.github.ixtf.vertx.readValue
import io.cloudevents.CloudEvent
import io.cloudevents.core.format.EventFormat
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.protobuf.ProtobufFormat.PROTO_CONTENT_TYPE
import io.netty.buffer.CompositeByteBuf
import io.rsocket.Payload
import io.vertx.core.buffer.Buffer

val CLOUD_EVENT_FORMAT: EventFormat by lazy {
  EventFormatProvider.getInstance().resolveFormat(PROTO_CONTENT_TYPE)!!
}

fun CloudEvent.toPayload(metadata: CompositeByteBuf? = null): Payload =
  CLOUD_EVENT_FORMAT.serialize(this).toPayload(metadata)

inline fun <reified T> CloudEvent.readValueOrNull(): T? =
  data?.toBytes()?.let { Buffer.buffer(it).readValue() }
