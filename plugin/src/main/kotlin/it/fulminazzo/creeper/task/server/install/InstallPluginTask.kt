package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.provider.plugin.PluginProvider
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to install one plugin in a server.
 *
 * @constructor Creates a new Install plugin task
 */
abstract class InstallPluginTask : DefaultTask() {

    @get:ServiceReference("pluginProvider")
    abstract val pluginProvider: PluginProvider<PluginRequest>

    @get:Input
    abstract val request: PluginRequest

    @get:OutputFile
    abstract val plugins: RegularFileProperty

    @TaskAction
    fun run() {
        val directory = plugins.get().asFile.parentFile.toPath()
        pluginProvider.handleRequest(directory, request)
    }

}