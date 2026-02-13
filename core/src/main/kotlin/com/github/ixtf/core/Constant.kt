package com.github.ixtf.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.StreamReadConstraints
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.MapperBuilder
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.dataformat.toml.TomlFactory
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ixtf.core.kit.extName
import jakarta.validation.Validation
import jakarta.validation.Validator
import kotlin.apply
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
    "json" -> JSON_MAPPER
    else -> JSON_MAPPER
  }

private fun MapperBuilder<*, *>.defaultConfigBuild(): ObjectMapper =
  findAndAddModules().build().apply {
    registerKotlinModule { enable(KotlinFeature.StrictNullChecks) }
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
    setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
  }

private fun build(mapper: ObjectMapper) =
  mapper.apply {
    registerKotlinModule { enable(KotlinFeature.StrictNullChecks) }
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
    setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
  }

val CBOR_MAPPER by lazy {
  val constraints = StreamReadConstraints.builder().maxStringLength(Int.MAX_VALUE).build()
  val factory = CBORFactory.builder().streamReadConstraints(constraints).build()
  CBORMapper.builder(factory).defaultConfigBuild()
}

val JSON_MAPPER by lazy {
  val constraints = StreamReadConstraints.builder().maxStringLength(Int.MAX_VALUE).build()
  val factory = JsonFactory.builder().streamReadConstraints(constraints).build()
  JsonMapper.builder(factory).defaultConfigBuild()
}

val YAML_MAPPER by lazy {
  val constraints = StreamReadConstraints.builder().maxStringLength(Int.MAX_VALUE).build()
  val factory = YAMLFactory.builder().streamReadConstraints(constraints).build()
  YAMLMapper.builder(factory).defaultConfigBuild()
}

val TOML_MAPPER by lazy {
  val constraints = StreamReadConstraints.builder().maxStringLength(Int.MAX_VALUE).build()
  val factory = TomlFactory.builder().streamReadConstraints(constraints).build()
  TomlMapper.builder(factory).defaultConfigBuild()
}

val VALIDATOR: Validator by lazy {
  Validation.byDefaultProvider()
    .configure()
    .messageInterpolator(ParameterMessageInterpolator())
    .buildValidatorFactory()
    .validator
}
