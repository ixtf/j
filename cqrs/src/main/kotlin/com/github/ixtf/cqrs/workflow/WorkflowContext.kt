package com.github.ixtf.cqrs.workflow

import com.github.ixtf.cqrs.Context

interface WorkflowContext : Context {
  val workflowId: String
}
