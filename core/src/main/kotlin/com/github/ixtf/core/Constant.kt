package com.github.ixtf.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ixtf.core.kit.extName
import jakarta.validation.Validation
import jakarta.validation.Validator
import java.util.*
import kotlin.apply
import kotlin.collections.forEach
import kotlin.jvm.java
import kotlin.text.lowercase
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator

/**
 * @param o 文件名，支持 json，yaml，toml
 * @return jackson map
 */
fun objectMap(o: String) =
  when (o.extName().lowercase()) {
    "yml",
    "yaml" -> YAML_MAPPER
    "toml" -> TOML_MAPPER
    "json" -> MAPPER
    else -> MAPPER
  }

private fun build(mapper: ObjectMapper) =
  mapper.apply {
    registerKotlinModule { enable(KotlinFeature.StrictNullChecks) }
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
    setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
  }

val MAPPER by lazy {
  //    val OBJECT_MAPPER = ObjectMapper(
  //        JsonFactoryBuilder()
  //            .streamReadConstraints(
  //                StreamReadConstraints.builder()
  //                    .maxStringLength(Int.MAX_VALUE) // your limit here
  //                    .build()
  //            )
  //            .build()
  //    )
  build(
    jsonMapper {
      findAndAddModules()
      // defaultLocale(Locale.CHINA)
      //      defaultTimeZone(TimeZone.getTimeZone(ZoneId.))
    }
  )
}

val YAML_MAPPER by lazy {
  build(
    YAMLMapper().apply { ServiceLoader.load(Module::class.java).forEach { registerModule(it) } }
  )
}

val TOML_MAPPER by lazy {
  build(
    TomlMapper().apply { ServiceLoader.load(Module::class.java).forEach { registerModule(it) } }
  )
}

val VALIDATOR: Validator by lazy {
  Validation.byDefaultProvider()
    .configure()
    .messageInterpolator(ParameterMessageInterpolator())
    .buildValidatorFactory()
    .validator
}
