package com.gitee.ixtf.guice

import com.gitee.ixtf.core.J
import com.gitee.ixtf.core.kotlinx.readJsonFile
import com.gitee.ixtf.guice.Jguice.JGUICE_CONFIG_LOCATIONS
import com.gitee.ixtf.guice.Jguice.JGUICE_CONFIG_PROFILE
import com.google.inject.AbstractModule
import com.google.inject.matcher.Matchers
import io.smallrye.config.*
import io.smallrye.config.SmallRyeConfig.SMALLRYE_CONFIG_PROFILE
import io.smallrye.config.common.MapBackedConfigSource
import io.smallrye.config.common.utils.ConfigSourceUtil
import java.net.URI
import java.util.*
import org.eclipse.microprofile.config.Config
import org.eclipse.microprofile.config.spi.ConfigSource

object JguiceConfigModule : AbstractModule(), ConfigSourceFactory {
  init {
    System.getProperty(JGUICE_CONFIG_PROFILE)?.let {
      System.setProperty(SMALLRYE_CONFIG_PROFILE, it)
    }
  }

  private class JguiceConfigSource(location: String, ordinal: Int) :
      MapBackedConfigSource(
          "JguiceConfigSource[source=$location]",
          when (J.extName(location)) {
            "json",
            "yaml",
            "yml",
            "toml" -> location.readJsonFile<Map<String, String>>()
            else -> ConfigSourceUtil.urlToMap(URI.create("file://$location").toURL())
          },
          ordinal)

  override fun configure() {
    super.configure()
    bind(Config::class.java)
        .toInstance(
            SmallRyeConfigBuilder().run {
              addDefaultInterceptors()
              addDefaultSources()
              withSources(JguiceConfigModule)
              withSecretKeys("secret")
              build()
            })
    bindListener(Matchers.any(), ConfigPropertyTypeListener)
  }

  override fun getConfigSources(context: ConfigSourceContext): Iterable<ConfigSource> {
    val value = context.getValue(JGUICE_CONFIG_LOCATIONS)
    if (value.value == null) {
      return emptyList()
    }
    return Converters.getImplicitConverter(String::class.java)
        .let { Converters.newArrayConverter(it, Array<String>::class.java) }
        .run { convert(value.value) }
        .map { JguiceConfigSource(it, value.configSourceOrdinal) }
        .toList()
  }
}
