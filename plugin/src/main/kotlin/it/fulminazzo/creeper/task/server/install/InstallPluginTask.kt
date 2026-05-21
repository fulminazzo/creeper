package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.provider.plugin.PluginProvider
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Task for performing the installation process of a [PluginRequest].
 * The actual process is defined by the different implementations.
 *
 * @constructor Creates a new Install plugin task
 * @see PluginProvider
 * @see PluginRequest
 */
abstract class InstallPluginTask : DefaultTask() {

    @get:ServiceReference("pluginProviderService")
    abstract val pluginProviderService: Property<PluginProviderService>

    @get:Input
    abstract val request: Property<PluginRequest>

    @get:InputFile
    abstract val pluginMetadata: RegularFileProperty

    @get:Internal
    abstract val pluginsDirectory: RegularFileProperty

    @TaskAction
    fun run() {
        val name = pluginMetadata.get().asFile.readText()
        val directory = pluginsDirectory.get().asFile
        pluginProviderService.get().provider.handleRequest(request.get(), directory.toPath(), name)
    }

}