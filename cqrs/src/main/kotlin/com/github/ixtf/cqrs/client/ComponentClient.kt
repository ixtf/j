package com.github.ixtf.cqrs.client

interface ComponentClient {
  fun forEventSourcedEntity(eventSourcedEntityId: String): EventSourcedEntityClient

  fun forKeyValueEntity(keyValueEntityId: String): KeyValueEntityClient

  fun forWorkflow(workflowId: String): WorkflowClient
}
