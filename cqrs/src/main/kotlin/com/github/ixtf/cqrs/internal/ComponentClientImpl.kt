package com.github.ixtf.cqrs.internal

import com.github.ixtf.cqrs.client.ComponentClient
import com.github.ixtf.cqrs.client.EventSourcedEntityClient
import com.github.ixtf.cqrs.client.KeyValueEntityClient
import com.github.ixtf.cqrs.client.WorkflowClient
import com.github.ixtf.cqrs.internal.codegen.ComponentInvoker
import com.github.ixtf.cqrs.internal.codegen.EventSourcedEntityFactory
import com.github.ixtf.cqrs.internal.codegen.KeyValueEntityFactory
import com.github.ixtf.cqrs.internal.codegen.WorkflowFactory
import com.github.ixtf.cqrs.internal.eventsourcedentity.EventSourcedEntityClientImpl
import com.github.ixtf.cqrs.internal.keyvalueentity.KeyValueEntityClientImpl
import com.github.ixtf.cqrs.internal.workflow.WorkflowClientImpl
import io.vertx.core.Vertx
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ComponentClientImpl
@Inject
constructor(
  private val vertx: Vertx,
  eventSourcedEntityFactories: Set<@JvmSuppressWildcards EventSourcedEntityFactory<*>>,
  keyValueEntityFactories: Set<@JvmSuppressWildcards KeyValueEntityFactory<*>>,
  workflowFactories: Set<@JvmSuppressWildcards WorkflowFactory<*>>,
  componentInvokers: Set<@JvmSuppressWildcards ComponentInvoker<*>>,
) : ComponentClient {
  private val eventSourcedEntityMap = eventSourcedEntityFactories.associateBy { it.componentClass }
  private val keyValueEntityMap = keyValueEntityFactories.associateBy { it.componentClass }
  private val workflowMap = workflowFactories.associateBy { it.componentClass }
  private val componentInvokerMap = componentInvokers.associateBy { it.componentClass }

  override fun forEventSourcedEntity(eventSourcedEntityId: String): EventSourcedEntityClient {
    require(eventSourcedEntityId.isNotBlank()) { "Event sourced entity id cannot be blank" }
    return EventSourcedEntityClientImpl(vertx, eventSourcedEntityMap, eventSourcedEntityId)
  }

  override fun forKeyValueEntity(keyValueEntityId: String): KeyValueEntityClient {
    require(keyValueEntityId.isNotBlank()) { "Key Value entity id cannot be blank" }
    return KeyValueEntityClientImpl(vertx, keyValueEntityMap, keyValueEntityId)
  }

  override fun forWorkflow(workflowId: String): WorkflowClient {
    require(workflowId.isNotBlank()) { "Workflow id cannot be blank" }
    return WorkflowClientImpl(vertx, workflowMap, workflowId)
  }
}
