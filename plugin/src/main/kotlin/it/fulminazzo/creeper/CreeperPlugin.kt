package it.fulminazzo.creeper

import org.gradle.api.Project
import org.gradle.api.Plugin
import tools.jackson.databind.ObjectMapper
import tools.jackson.dataformat.javaprop.JavaPropsMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.kotlinModule
import java.nio.file.Path

/**
 * A simple 'hello world' plugin.
 */
class CreeperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Register a task
        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello, world!")
            }
        }
    }

    companion object {
        /**
         * The global cache directory.
         */
        internal val CACHE_DIRECTORY
            get() = Path.of(System.getProperty("user.home"), ".gradle", "caches", ProjectInfo.NAME)

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

    }

}
