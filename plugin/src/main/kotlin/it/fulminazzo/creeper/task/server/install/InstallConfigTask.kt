package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.server.spec.ServerSpec
import it.fulminazzo.creeper.service.provider.ConfigProviderService
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import tools.jackson.module.kotlin.readValue
import kotlin.io.path.extension
import kotlin.io.path.fileSize

/**
 * Task to install a server configuration file.
 *
 * @constructor Creates a new Install config task
 */
abstract class InstallConfigTask : DefaultTask() {

    @get:ServiceReference("configProviderService")
    abstract val configProviderService: ConfigProviderService

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
        val path = configProviderService.configProvider.get(
            file.name,
            spec.type,
            spec.version,
            file.parentFile.toPath()
        )
        val mapper = CreeperPlugin.getMapper(path.extension)
        val currentConfig =
            if (path.fileSize() > 0) mapper.readValue<MutableMap<String, Any>>(path.toFile())
            else mutableMapOf()
        action.get().apply(currentConfig, spec)
        mapper.writeValue(path, currentConfig)
    }

}