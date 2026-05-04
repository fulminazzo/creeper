package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to fetch the metadata of one plugin.
 *
 * @constructor Creates a new Fetch plugin metadata task
 */
abstract class FetchPluginMetadataTask : DefaultTask() {

    @get:ServiceReference("pluginProviderService")
    abstract val pluginProviderService: Property<PluginProviderService>

    @get:Input
    abstract val request: Property<PluginRequest>

    @get:OutputFile
    abstract val pluginMetadata: RegularFileProperty

    @TaskAction
    fun run() {
        val name = pluginProviderService.get().provider.getName(request.get())
        val file = pluginMetadata.get().asFile
        file.parentFile.mkdirs()
        file.writeText(name)
    }

}