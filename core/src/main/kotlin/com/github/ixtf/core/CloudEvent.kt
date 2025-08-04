package com.github.ixtf.core

import com.fasterxml.jackson.module.kotlin.readValue
import io.cloudevents.CloudEvent
import io.cloudevents.core.format.EventFormat
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.protobuf.ProtobufFormat.PROTO_CONTENT_TYPE
import java.nio.charset.StandardCharsets.UTF_8

val CLOUD_EVENT_FORMAT: EventFormat by lazy {
  EventFormatProvider.getInstance().resolveFormat(PROTO_CONTENT_TYPE)!!
}

fun CloudEvent.serialize(): ByteArray = CLOUD_EVENT_FORMAT.serialize(this)

fun CloudEvent.bytes(): ByteArray = data!!.toBytes()

inline fun <reified T> CloudEvent.readJson() = MAPPER.readValue<T>(bytes())

fun CloudEvent.string() = bytes().toString(UTF_8)
