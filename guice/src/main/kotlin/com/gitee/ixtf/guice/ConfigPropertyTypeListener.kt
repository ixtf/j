package com.gitee.ixtf.guice

import com.google.inject.MembersInjector
import com.google.inject.TypeLiteral
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import io.smallrye.config.ConfigMapping
import io.smallrye.config.ConfigMappings.ConfigClassWithPrefix
import io.smallrye.config.ConfigMappings.registerConfigProperties
import io.smallrye.config.SmallRyeConfig
import java.lang.reflect.Field
import org.eclipse.microprofile.config.inject.ConfigProperty

object ConfigPropertyTypeListener : TypeListener {
  override fun <T> hear(typeLiteral: TypeLiteral<T>, typeEncounter: TypeEncounter<T>) {
    var clazz = typeLiteral.rawType
    while (clazz != null) {
      clazz.declaredFields
          .filter { it.isAnnotationPresent(ConfigProperty::class.java) }
          .forEach { typeEncounter.register(ConfigPropertyMembersInjector<T>(it)) }
      clazz = clazz.superclass
    }
  }

  private class ConfigPropertyMembersInjector<T>(private val field: Field) : MembersInjector<T> {
    override fun injectMembers(t: T) {
      val config = Jguice.config().unwrap(SmallRyeConfig::class.java)
      val value =
          field.type.getAnnotation(ConfigMapping::class.java)?.run {
            registerConfigProperties(config, setOf(ConfigClassWithPrefix(field.type, prefix)))
            config.getConfigMapping(field.type)
          }
              ?: field.getAnnotation(ConfigProperty::class.java).run {
                config.getValue(name, field.type)
              }
      field.isAccessible = true
      field.set(t, value)
    }
  }
}
