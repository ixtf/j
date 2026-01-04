package com.github.ixtf.cqrs.internal.codegen

import com.github.ixtf.cqrs.client.ComponentClient
import com.github.ixtf.cqrs.internal.ComponentClientImpl
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds

@Module
interface CodegenCqrsModule {
  @Binds fun bind(impl: ComponentClientImpl): ComponentClient

  @Multibinds fun eventSourcedEntity(): Set<@JvmSuppressWildcards EventSourcedEntityFactory<*>>

  @Multibinds fun keyValueEntity(): Set<@JvmSuppressWildcards KeyValueEntityFactory<*>>

  @Multibinds fun workflow(): Set<@JvmSuppressWildcards WorkflowFactory<*>>

  @Multibinds fun componentInvoker(): Set<@JvmSuppressWildcards ComponentInvoker<*>>
}
