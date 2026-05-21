package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * A task to inject the `tester` module runner dependency in the server.
 *
 * The task will do two actions:
 * - Inject the [GitHubPluginRequest] in the specification;
 * - Write or update the plugin configuration file with the `build` directory path and any required dependency
 * for the tests to run.
 *
 * @constructor Creates a new Inject test runner dependency
 */
//TODO: This class is not tested yet, since it requires the actual release of the `tester` module
abstract class InjectTestRunnerDependencyTask : DefaultTask() {

    @get:Internal
    abstract val project: Project

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:OutputFile
    abstract val pluginConfigurationFile: RegularFileProperty

    @TaskAction
    fun run() {
        logger.info("Injecting Tests runner JAR as a plugin request")
        specification.get().plugins.add(GitHubPluginRequest(
            ProjectInfo.GROUP.substringAfterLast("."),
            ProjectInfo.NAME,
            ProjectInfo.VERSION,
            "${
                ProjectInfo.NAME.lowercase().replaceFirstChar { it.uppercase() }
            }Tester-${ProjectInfo.VERSION}.jar"
        ))

        logger.info("Writing plugin configuration file")
        val configurationFile = pluginConfigurationFile.get().asFile

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

    private companion object {
        private val CONFIGURATIONS = listOf("runtimeClasspath", "integrationTestRuntimeClasspath")

        private fun getResolvedArtifacts(configuration: Configuration) =
            try {
                configuration.resolvedConfiguration.resolvedArtifacts.map { it.file }
            } catch (_: Exception) {
                emptyList()
            }

    }

}