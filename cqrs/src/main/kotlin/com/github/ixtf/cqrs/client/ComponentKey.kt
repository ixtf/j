package com.github.ixtf.cqrs.client

import com.github.ixtf.cqrs.eventsourcedentity.EventSourcedEntity
import com.github.ixtf.cqrs.internal.eventsourcedentity.EventSourcedEntityKey
import com.github.ixtf.cqrs.internal.keyvalueentity.KeyValueEntityKey
import com.github.ixtf.cqrs.internal.workflow.WorkflowKey
import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import com.github.ixtf.cqrs.workflow.Workflow

interface ComponentKey<T> {
  companion object {
    fun <T : EventSourcedEntity<*, *>> forEventSourcedEntity(
      clazz: Class<T>,
      eventSourcedEntityId: String,
    ): ComponentKey<T> = EventSourcedEntityKey(clazz, eventSourcedEntityId)

    fun <T : KeyValueEntity<*>> forKeyValueEntity(
      clazz: Class<T>,
      keyValueEntityId: String,
    ): ComponentKey<T> = KeyValueEntityKey(clazz, keyValueEntityId)

    fun <T : Workflow<*>> forWorkflow(clazz: Class<T>, workflowId: String): ComponentKey<T> =
      WorkflowKey(clazz, workflowId)
  }

  suspend fun invalidate()
}
