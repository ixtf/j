package com.github.ixtf.cqrs.internal.codegen

import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntity
import com.github.ixtf.cqrs.keyvalueentity.KeyValueEntityContext

interface KeyValueEntityFactory<T : KeyValueEntity<*>> : ComponentFactory<T> {
  fun create(context: KeyValueEntityContext): T
}
