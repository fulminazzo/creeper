package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.service.provider.ConfigProviderService
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.extension.spec.ServerSpec
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.readText

/**
 * Task to install a server configuration file.
 *
 * @constructor Creates a new Install config task
 */
abstract class InstallConfigTask : DefaultTask() {

    @get:ServiceReference("configProviderService")
    abstract val configProviderService: Property<ConfigProviderService>

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
        val path = configProviderService.get().provider.get(
            file.name,
            spec.type,
            spec.version,
            file.parentFile.toPath()
        )
        val mapper = CreeperPlugin.getMapper(path.extension)
        val currentConfig =
            // Reading in ISO_8859_1 to support section signs
            if (path.fileSize() > 0) mapper.readValue<MutableMap<String, Any>>(path.readText(Charsets.ISO_8859_1))
            else mutableMapOf()
        action.get().apply(currentConfig, spec)
        mapper.writeValue(path.toFile(), currentConfig)
    }

}