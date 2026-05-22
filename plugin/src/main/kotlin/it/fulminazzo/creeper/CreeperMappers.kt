package it.fulminazzo.creeper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule

internal val JSON_MAPPER = jacksonObjectMapper()
internal val YAML_MAPPER = YAMLMapper.builder().addModule(kotlinModule()).build()
internal val PROPERTIES_MAPPER = JavaPropsMapper.builder().addModule(kotlinModule()).build()

/**
 * Gets an appropriate Jackson mapper for the given format.
 *
 * @param format the format of the mapper (file extension)
 * @return the mapper
 */
internal fun getMapper(format: String): ObjectMapper = when (format) {
    "json" -> JSON_MAPPER
    "yaml", "yml" -> YAML_MAPPER
    "properties" -> PROPERTIES_MAPPER
    else -> throw IllegalArgumentException("Unsupported format: $format")
}
