package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.task.server.install.InjectTestRunnerRequestTask.Companion.PLUGIN_REQUEST
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * A task to inject the `test-runner` JAR request in the server.
 *
 * The task will do two actions:
 * - Inject the [GitHubPluginRequest] in the specification;
 * - Write or update the plugin configuration file with the `build` directory path and any required dependency
 * for the tests to run.
 *
 * @constructor Creates a new Inject test runner plugin request task
 */
//TODO: This class is not completely tested yet, since it requires the actual release of the `test-runner` module
abstract class InjectTestRunnerRequestTask : DefaultTask() {

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:OutputFile
    abstract val pluginConfigurationFile: RegularFileProperty

    @TaskAction
    fun run() {
        logger.info("Injecting Tests runner JAR as a plugin request")
        specification.get().plugins.add(PLUGIN_REQUEST)

        logger.info("Writing plugin configuration file")
        val configurationFile = pluginConfigurationFile.get().asFile
        configurationFile.parentFile.mkdirs()
        configurationFile.createNewFile()

        val buildDir = project.layout.buildDirectory.get().asFile
        val dependencies = CONFIGURATIONS
            .map { project.configurations.getByName(it) }
            .flatMap { getResolvedArtifacts(it) }
            .distinct()

        val data = mapOf(
            "build-directory-path" to buildDir.absolutePath,
            "dependencies" to dependencies
        )

        CreeperPlugin.YAML_MAPPER.writeValue(configurationFile, data)
    }

    internal companion object {
        /**
         * The plugin request for installing the `test-runner` module in the `plugins` directory.
         */
        internal var PLUGIN_REQUEST: PluginRequest = GitHubPluginRequest(
            ProjectInfo.GROUP.substringAfterLast("."),
            ProjectInfo.NAME,
            ProjectInfo.VERSION,
            "${
                ProjectInfo.NAME.lowercase().replaceFirstChar { it.uppercase() }
            }Tester-${ProjectInfo.VERSION}.jar"
        )

        private val CONFIGURATIONS = listOf("runtimeClasspath", "integrationTestRuntimeClasspath")

        /**
         * Updates the [PLUGIN_REQUEST] for tests.
         */
        internal fun testMode() {
            val testRunnerJar = File("").absoluteFile.toPath().parent
                .resolve("tester")
                .resolve("build")
                .resolve("libs")
                .resolve("${ProjectInfo.NAME}Tester-${ProjectInfo.VERSION}.jar")
            PLUGIN_REQUEST = LocalPluginRequest(testRunnerJar, true)
        }

        private fun getResolvedArtifacts(configuration: Configuration) =
            try {
                configuration.resolvedConfiguration.resolvedArtifacts.map { it.file }
            } catch (_: Exception) {
                emptyList()
            }

    }

}