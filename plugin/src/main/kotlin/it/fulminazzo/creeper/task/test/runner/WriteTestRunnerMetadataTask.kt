package it.fulminazzo.creeper.task.test.runner

import it.fulminazzo.creeper.ProjectInfo
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Writes the necessary data for the test runner to build.
 *
 * @constructor Creates a new Write test runner metadata task
 */
abstract class WriteTestRunnerMetadataTask : DefaultTask() {

    @get:OutputFile
    abstract val buildFile: RegularFileProperty

    @TaskAction
    fun run() {
        val targetConfigurations = setOf(
            "implementation",
            "runtimeOnly",
            "integrationTestImplementation",
            "integrationTestRuntimeOnly"
        )

        val dependencies = targetConfigurations
            .mapNotNull { name -> project.configurations.findByName(name) }
            .flatMap { config -> resolveSafely(config) }
            .distinct()

        logger.lifecycle("Collected ${dependencies.size} dependencies")

        //TODO: Java version
        val build = buildFile.get().asFile
        val lines = build.readLines().map { line ->
            if (line.startsWith("val implementationDependencies"))
                "val implementationDependencies = listOf(${dependencies.joinToString(", ")})"
            else if (line.startsWith("val parentGroup")) "${ProjectInfo.GROUP}:${ProjectInfo.NAME}"
            else if (line.startsWith("val parentVersion")) ProjectInfo.VERSION
            else line
        }
        build.writeText(lines.joinToString("\n"))
    }

    private fun resolveSafely(config: Configuration): List<String> = try {
        config
            .resolvedConfiguration
            .resolvedArtifacts
            .map { "${it.moduleVersion.id.group}:${it.name}:${it.moduleVersion.id.version}" }
    } catch (e: Exception) {
        logger.warn("Could not resolve configuration '${config.name}': ${e.message}")
        emptyList()
    }

}