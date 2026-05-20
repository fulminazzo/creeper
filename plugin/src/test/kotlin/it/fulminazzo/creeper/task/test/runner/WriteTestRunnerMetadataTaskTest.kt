package it.fulminazzo.creeper.task.test.runner

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.task.TaskTestHelper
import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.junit.jupiter.api.BeforeEach
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class WriteTestRunnerMetadataTaskTest : TaskTestHelper() {

    @BeforeEach
    fun setup() {
        project.plugins.apply(JavaPlugin::class.java)

        project.configurations.create("integrationTestImplementation")
        project.configurations.create("integrationTestRuntimeOnly")
        project.configurations.create("integrationTestRuntimeClasspath") {
            it.isCanBeResolved = true
            it.isCanBeConsumed = false
            it.extendsFrom(
                project.configurations.getByName("integrationTestImplementation"),
                project.configurations.getByName("integrationTestRuntimeOnly")
            )
        }

        project.repositories.mavenCentral()

        project.dependencies.add(
            "implementation",
            "org.junit.jupiter:junit-jupiter-api:5.8.2"
        )
        project.dependencies.add(
            "integrationTestImplementation",
            "org.junit.jupiter:junit-jupiter-engine:5.8.2"
        )
        project.dependencies.add(
            "runtimeOnly",
            "org.junit.platform:junit-platform-launcher:1.8.2"
        )
        project.dependencies.add(
            "integrationTestRuntimeOnly",
            "org.junit.platform:junit-platform-launcher:1.8.2"
        )

        resetBuildFile()
    }

    @Test
    fun `test that WriteTestRunnerMetadataTask is able to obtain Java language version from toolchains`() {
        val toolchain = project.extensions.getByType(JavaPluginExtension::class.java).toolchain
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))

        val task = createTask(WriteTestRunnerMetadataTask::class.java) { task ->
            task.buildFile.set(BUILD_FILE)
        }

        task.run()

        val buildFile = task.project.projectDir.resolve(BUILD_FILE)
        assertTrue(buildFile.exists(), "Build file should exist")

        val lines = buildFile.readLines()
        assertContentEquals(
            listOf(
                "// Mock build.gradle.kts file for testing purposes",
                "val compileJavaVersion = JavaLanguageVersion.of(17)",
                "val gradleJavaVersion = JavaLanguageVersion.of(17)",
                "val implementationDependencies = listOf(" +
                        "\"org.junit.platform:junit-platform-launcher:1.8.2\", " +
                        "\"org.junit.platform:junit-platform-engine:1.8.2\", " +
                        "\"org.junit.platform:junit-platform-commons:1.8.2\", " +
                        "\"org.junit.jupiter:junit-jupiter-api:5.8.2\", " +
                        "\"org.opentest4j:opentest4j:1.2.0\", " +
                        "\"org.junit.jupiter:junit-jupiter-engine:5.8.2\"" +
                        ")",
                "val parentGroup = \"${ProjectInfo.GROUP}.${ProjectInfo.NAME}\"",
                "val parentVersion = \"${ProjectInfo.VERSION}\"",
                "// Should be ignored"
            ),
            lines
        )
    }

    @Test
    fun `test that WriteTestRunnerMetadataTask is able to obtain Java language version from target compatibility`() {
        project.extensions.getByType(JavaPluginExtension::class.java).targetCompatibility = JavaVersion.toVersion("17")

        val task = createTask(WriteTestRunnerMetadataTask::class.java) { task ->
            task.buildFile.set(BUILD_FILE)
        }

        task.run()

        val buildFile = task.project.projectDir.resolve(BUILD_FILE)
        assertTrue(buildFile.exists(), "Build file should exist")

        val lines = buildFile.readLines()
        assertContentEquals(
            listOf(
                "// Mock build.gradle.kts file for testing purposes",
                "val compileJavaVersion = JavaLanguageVersion.of(17)",
                "val gradleJavaVersion = JavaLanguageVersion.of(17)",
                "val implementationDependencies = listOf(" +
                        "\"org.junit.platform:junit-platform-launcher:1.8.2\", " +
                        "\"org.junit.platform:junit-platform-engine:1.8.2\", " +
                        "\"org.junit.platform:junit-platform-commons:1.8.2\", " +
                        "\"org.junit.jupiter:junit-jupiter-api:5.8.2\", " +
                        "\"org.opentest4j:opentest4j:1.2.0\", " +
                        "\"org.junit.jupiter:junit-jupiter-engine:5.8.2\"" +
                        ")",
                "val parentGroup = \"${ProjectInfo.GROUP}.${ProjectInfo.NAME}\"",
                "val parentVersion = \"${ProjectInfo.VERSION}\"",
                "// Should be ignored"
            ),
            lines
        )
    }

    private fun resetBuildFile() {
        val buildFile = project.projectDir.resolve(BUILD_FILE)
        buildFile.delete()
        buildFile.parentFile.mkdirs()
        buildFile.createNewFile()
        buildFile.writeText(
            """// Mock build.gradle.kts file for testing purposes
            |val compileJavaVersion = null
            |val gradleJavaVersion = null
            |val implementationDependencies = null
            |val parentGroup = null
            |val parentVersion = null
            |// Should be ignored
        """.trimMargin()
        )
    }

    private companion object {
        private val BUILD_FILE = File("build/resources/test/task/test/runner/build.gradle.kts")

    }

}