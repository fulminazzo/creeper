package it.fulminazzo.creeper.task.server

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import it.fulminazzo.creeper.task.server.InstallServerRunnerTask.Companion.PLUGIN_REQUEST
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task for performing the installation process of the `server-runner` module.
 *
 * @constructor Creates a new Install server runner task
 */
abstract class InstallServerRunnerTask : DefaultTask() {

    @get:ServiceReference("pluginProviderService")
    abstract val pluginProviderService: Property<PluginProviderService>

    @get:OutputFile
    abstract val serverRunner: RegularFileProperty

    @TaskAction
    fun run() {
        val file = serverRunner.get().asFile
        val name = file.name
        val directory = file.parentFile
        pluginProviderService.get().provider.handleRequest(PLUGIN_REQUEST, directory.toPath(), name)
    }

    internal companion object {
        internal const val FILE_NAME = "${ProjectInfo.NAME}-server-runner-${ProjectInfo.VERSION}.jar"

        /**
         * The plugin request for installing the `server-runner` module in the `plugins` directory.
         */
        internal var PLUGIN_REQUEST: PluginRequest = GitHubPluginRequest(
            ProjectInfo.GROUP.substringAfterLast("."),
            ProjectInfo.NAME,
            ProjectInfo.VERSION,
            FILE_NAME
        )

        /**
         * Updates the [PLUGIN_REQUEST] for tests.
         */
        internal fun testMode() {
            val testRunnerJar = File("").absoluteFile.toPath().parent
                .resolve("server-runner")
                .resolve("build")
                .resolve("libs")
                .resolve(FILE_NAME)
            PLUGIN_REQUEST = LocalPluginRequest(testRunnerJar, true)
        }

    }

}