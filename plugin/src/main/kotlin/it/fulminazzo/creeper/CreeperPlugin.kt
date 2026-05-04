package it.fulminazzo.creeper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import it.fulminazzo.creeper.service.PlayerResolverService
import it.fulminazzo.creeper.service.downloader.CachedDownloaderService
import it.fulminazzo.creeper.service.provider.ConfigProviderService
import it.fulminazzo.creeper.service.provider.JarProviderService
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.nio.file.Paths

/**
 * A simple 'hello world' plugin.
 */
class CreeperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val gradle = project.gradle
        // SERVICES
        val sharedServices = gradle.sharedServices
        val downloadService = sharedServices
            .registerIfAbsent("downloaderService", CachedDownloaderService::class.java)
        sharedServices.registerIfAbsent("playerResolverService", PlayerResolverService::class.java)
        sharedServices.registerIfAbsent("jarProviderService", JarProviderService::class.java) {
            it.parameters.downloader.set(downloadService)
        }
        sharedServices.registerIfAbsent("configProviderService", ConfigProviderService::class.java) {
            it.parameters.downloader.set(downloadService)
        }
        sharedServices.registerIfAbsent("pluginProviderService", PluginProviderService::class.java) {
            it.parameters.downloader.set(downloadService)
        }

        // EXTENSIONS
        val serversConfigExtension = project.extensions.create(
            ProjectInfo.NAME.lowercase(),
            ServersConfigurationExtension::class.java
        )

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
            get() = Paths.get(System.getProperty("user.home"), ".gradle", "caches", ProjectInfo.NAME)

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
