package com.github.ixtf.cqrs.workflow

import com.github.ixtf.cqrs.client.ComponentKey
import com.github.ixtf.cqrs.internal.workflow.WorkflowContextImpl.Companion.ebAddress
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle
import kotlin.getValue

abstract class Workflow<S>(protected val context: WorkflowContext) : BaseCoroutineVerticle() {
  val workflowId by context::workflowId
  val componentKey by lazy { ComponentKey.forWorkflow(this::class.java, workflowId) }
  private val ebAddress by lazy { this.ebAddress() }
}
