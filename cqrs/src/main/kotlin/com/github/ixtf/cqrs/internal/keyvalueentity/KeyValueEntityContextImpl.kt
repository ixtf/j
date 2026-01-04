package com.github.ixtf.cqrs.internal.keyvalueentity

import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntityContext

internal class KeyValueEntityContextImpl(
  override val entityId: String,
  override val selfRegion: String?,
) : KeyValueEntityContext {
  companion object {
    private const val EB_ADDRESS_KEY_VALUE_ENTITY = "__eb:cqrs:KeyValueEntity__"

    fun <T : KeyValueEntity<*>> T.ebAddress() = ebAddress(this::class.java)

    fun ebAddress(clazz: Class<out KeyValueEntity<*>>) = "$EB_ADDRESS_KEY_VALUE_ENTITY${clazz.name}"
  }
}
