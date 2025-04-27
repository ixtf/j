package com.gitee.ixtf.guice

import com.gitee.ixtf.guice.kotlinx.get
import com.google.inject.Guice.createInjector
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.Stage.PRODUCTION
import com.google.inject.name.Names
import kotlin.apply
import kotlin.reflect.KProperty
import org.eclipse.microprofile.config.Config

object Jguice {
  const val JGUICE_CONFIG_PROFILE = "jguice.config.profile"
  const val JGUICE_CONFIG_LOCATIONS = "jguice.config.locations"
  private lateinit var INJECTOR: Injector

  /**
   * 初始化guice
   *
   * @param injector 外部注入
   */
  @Synchronized
  @JvmStatic
  fun init(injector: Injector) {
    INJECTOR = injector
  }

  /**
   * 初始化guice
   *
   * @param modules 所有模块
   */
  @Synchronized
  @JvmStatic
  fun init(vararg modules: Module) {
    INJECTOR = createInjector(PRODUCTION, JguiceConfigModule, *modules)
  }

  /**
   * 获取注入类型实例
   *
   * @param type 注入类型
   * @param <T> 泛型
   * @return 实例 </T>
   */
  @JvmStatic fun <T> getInstance(type: Class<T>): T = INJECTOR.getInstance(Key.get(type))

  @JvmStatic fun <T> getInstance(key: Key<T>): T = INJECTOR.getInstance(key)

  @JvmStatic
  fun <T> getInstance(type: Class<T>, name: String): T =
      INJECTOR.getInstance(Key.get(type, Names.named(name)))

  /**
   * 获取注入类型实例
   *
   * @param type 注入类型
   * @param annotation 限定类
   * @param <T> 泛型
   * @return 实例 </T>
   */
  @JvmStatic
  fun <T> getInstance(type: Class<T>, annotation: Annotation): T =
      INJECTOR.getInstance(Key.get(type, annotation))

  @JvmStatic
  fun <T> getInstance(type: Class<T>, annotationType: Class<out Annotation>): T =
      INJECTOR.getInstance(Key.get(type, annotationType))

  /**
   * 注入注解
   *
   * @param o 实例
   * @param <T> 泛型
   * @return 输入实例 </T>
   */
  @JvmStatic fun <T> injectMembers(o: T) = o.apply { INJECTOR.injectMembers(o) }

  @JvmStatic fun config() = getInstance(object : Key<Config>() {})

  inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T = get<T>()
}
