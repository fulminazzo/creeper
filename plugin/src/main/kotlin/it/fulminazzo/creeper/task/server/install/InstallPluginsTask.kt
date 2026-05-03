package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.provider.plugin.PluginProvider
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

/**
 * Task to install plugins in a server.
 *
 * @constructor Creates a new Install plugins task
 */
abstract class InstallPluginsTask : DefaultTask() {

    @get:ServiceReference("pluginProvider")
    abstract val pluginProvider: PluginProvider<PluginRequest>

    @get:Input
    abstract val requests: Property<List<PluginRequest>>

    @get:Input
    abstract val pluginsDirectory: RegularFileProperty

    @get:OutputFiles
    abstract val plugins: ConfigurableFileCollection

    @TaskAction
    fun run() {
        logger.lifecycle("Installing plugins")
        val directory = Path.of(pluginsDirectory.get().asFile.toURI())
        requests.get().forEach { request ->
            val path = pluginProvider.handleRequest(directory, request).join()
            plugins.from(path)
        }
        logger.lifecycle("Completed download of ${plugins.files.size} plugins")
    }

}