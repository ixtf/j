package com.github.ixtf.cqrs.internal.eventsourcedentity

import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity
import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntityContext

internal class EventSourcedEntityContextImpl(
  override val entityId: String,
  override val selfRegion: String?,
) : EventSourcedEntityContext {
  companion object {
    private const val EB_ADDRESS_EVENT_SOURCED_ENTITY = "__eb:cqrs:EventSourcedEntity__"

    fun <T : EventSourcedEntity<*, *>> T.ebAddress() = ebAddress(this::class.java)

    fun ebAddress(clazz: Class<out EventSourcedEntity<*, *>>) =
      "$EB_ADDRESS_EVENT_SOURCED_ENTITY${clazz.name}"
  }
}
