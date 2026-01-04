package com.github.ixtf.cqrs.internal.codegen

import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity
import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntityContext

interface EventSourcedEntityFactory<T : EventSourcedEntity<*, *>> : ComponentFactory<T> {
  fun create(context: EventSourcedEntityContext): T
}
