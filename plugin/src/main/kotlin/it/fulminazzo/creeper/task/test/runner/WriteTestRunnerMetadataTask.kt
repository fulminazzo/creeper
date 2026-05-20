package it.fulminazzo.creeper.task.test.runner

import it.fulminazzo.creeper.ProjectInfo
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Modifies the `build.gradle` file of the `tester` module to conform to our requirements.
 * The "requirements" are defined as the following:
 * - Project group and name should be the same as the plugin ones;
 * - Any `implementation`, `runtimeOnly`, `integrationTestImplementation` or `integrationTestRuntimeOnly` dependency
 * must be delivered as `implementation` to the `tester` module, so that it can be packaged in the final JAR;
 * - The Java compile version of the `tester` should match the one project.
 *
 * @constructor Creates a new Write test runner metadata task
 */
abstract class WriteTestRunnerMetadataTask : DefaultTask() {

    @get:OutputFile
    abstract val buildFile: RegularFileProperty

    @TaskAction
    fun run() {
        val javaExtension = project.extensions.findByType(JavaPluginExtension::class.java)
            ?: throw GradleException("Java plugin not applied to the project")

        val javaVersion = javaExtension.toolchain.languageVersion.orNull?.asInt()
            ?: javaExtension.targetCompatibility.majorVersion.toInt()
        logger.lifecycle("Detected Java version: $javaVersion")

        val dependencies = REQUIRED_CONFIGURATIONS
            .mapNotNull { name -> project.configurations.findByName(name) }
            .flatMap { config -> resolveSafely(config) }
            .distinct()

        logger.lifecycle("Collected ${dependencies.size} dependencies")

        val build = buildFile.get().asFile
        val lines = build.readLines().map { line ->
            if (line.startsWith("val compileJavaVersion"))
                "val compileJavaVersion = JavaLanguageVersion.of($javaVersion)"
            else if (line.startsWith("val gradleJavaVersion"))
                "val gradleJavaVersion = JavaLanguageVersion.of($javaVersion)"
            else if (line.startsWith("val implementationDependencies"))
                "val implementationDependencies = listOf(${dependencies.joinToString(", ") { "\"$it\"" }})"
            else if (line.startsWith("val parentGroup")) "val parentGroup = \"${ProjectInfo.GROUP}.${ProjectInfo.NAME}\""
            else if (line.startsWith("val parentVersion")) "val parentVersion = \"${ProjectInfo.VERSION}\""
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

    private companion object {
        private val REQUIRED_CONFIGURATIONS = setOf("runtimeClasspath", "integrationTestRuntimeClasspath")

    }

}