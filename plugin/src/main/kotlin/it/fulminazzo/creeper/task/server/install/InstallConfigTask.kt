package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.provider.ConfigProvider
import it.fulminazzo.creeper.server.spec.ServerSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import tools.jackson.databind.ObjectMapper
import tools.jackson.dataformat.javaprop.JavaPropsMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import kotlin.io.path.extension
import kotlin.io.path.fileSize

/**
 * Task to install a server configuration file.
 *
 * @constructor Creates a new Install config task
 */
abstract class InstallConfigTask : DefaultTask() {

    @get:ServiceReference("configProvider")
    abstract val configProvider: ConfigProvider

    @get:Internal
    abstract val action: Property<ConfigAction>

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:OutputFile
    abstract val configFile: RegularFileProperty

    @TaskAction
    fun run() {
        val spec = specification.get()
        val file = configFile.get().asFile
        logger.lifecycle("Installing server configuration: ${file.name}")
        val path = configProvider.get(
            file.name,
            spec.type,
            spec.version,
            file.parentFile.toPath()
        ).join()
        val mapper = getMapper(path.extension)
        val currentConfig =
            if (path.fileSize() > 0) mapper.readValue<MutableMap<String, Any>>(path.toFile())
            else mutableMapOf()
        action.get().apply(currentConfig, spec)
        mapper.writeValue(path, currentConfig)
    }

    private companion object {
        private val JSON_MAPPER = jacksonObjectMapper()
        private val YAML_MAPPER = YAMLMapper.builder().addModule(kotlinModule()).build()
        private val PROPERTIES_MAPPER = JavaPropsMapper.builder().addModule(kotlinModule()).build()

        /**
         * Gets an appropriate Jackson mapper for the given format.
         *
         * @param format the format of the mapper (file extension)
         * @return the mapper
         */
        fun getMapper(format: String): ObjectMapper = when (format) {
            "json" -> JSON_MAPPER
            "yaml", "yml" -> YAML_MAPPER
            "properties" -> PROPERTIES_MAPPER
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }

    }

}