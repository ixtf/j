package com.github.ixtf.cqrs.internal.codegen

import com.github.ixtf.cqrs.workflow.Workflow
import com.github.ixtf.cqrs.workflow.WorkflowContext

interface WorkflowFactory<T : Workflow<*>> : ComponentFactory<T> {
  fun create(context: WorkflowContext): T
}
