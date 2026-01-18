package com.github.ixtf.cqrs.keyvalueentity

import com.github.ixtf.cqrs.client.ComponentKey
import com.github.ixtf.cqrs.internal.keyvalueentity.KeyValueEntityContextImpl.Companion.ebAddress
import com.github.ixtf.vertx.verticle.BaseCoroutineVerticle

abstract class KeyValueEntity<S>(private val context: KeyValueEntityContext) :
  BaseCoroutineVerticle() {
  val entityId by context::entityId
  val componentKey by lazy { ComponentKey.forKeyValueEntity(this::class.java, entityId) }
  private val ebAddress by lazy { this.ebAddress() }
}
