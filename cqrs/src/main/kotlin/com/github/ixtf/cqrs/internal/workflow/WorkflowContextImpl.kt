package com.github.ixtf.cqrs.internal.workflow

import com.github.ixtf.cqrs.workflow.Workflow
import com.github.ixtf.cqrs.workflow.WorkflowContext

internal class WorkflowContextImpl(
  override val workflowId: String,
  override val selfRegion: String?,
) : WorkflowContext {
  companion object {
    private const val EB_ADDRESS_WORK_FLOW = "__eb:cqrs:Workflow__"

    fun <T : Workflow<*>> T.ebAddress() = ebAddress(this::class.java)

    fun ebAddress(clazz: Class<out Workflow<*>>) = "$EB_ADDRESS_WORK_FLOW${clazz.name}"
  }
}
