package com.github.ixtf.cqrs

interface EntityContext : Context {
  val entityId: String
}
