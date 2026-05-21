package it.fulminazzo.creeper.task.server.install

import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.task.TaskIntegrationTestHelper
import org.gradle.api.plugins.JavaPlugin
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InjectTestRunnerTaskIntegrationTestRequest : TaskIntegrationTestHelper() {

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
    }

    @Test
    fun `test that InjectTestRunnerDependencyTask correctly injects plugin request and updates configuration`() {
        val spec = MinecraftServerSpec()
        val configFile = project.file("build/resources/integrationTest/server/plugin/CreeperTester/config.yml")

        val task = createTask(InjectTestRunnerRequestTask::class.java) { task ->
            task.specification.set(spec)
            task.pluginConfigurationFile.set { configFile }
        }
        task.run()

        assertContentEquals(
            listOf(InjectTestRunnerRequestTask.PLUGIN_REQUEST),
            spec.plugins
        )

        assertTrue(configFile.exists(), "Configuration file ${configFile.path} should exist")

        val config = CreeperPlugin.YAML_MAPPER.readValue<Map<String, Any>>(configFile)

        val buildDirectory = project.file("build").absolutePath
        assertEquals(
            buildDirectory,
            config["build-directory-path"],
            "build-directory-path should be set to $buildDirectory"
        )

        val dependencies = config["dependencies"]
        assertIs<List<String>>(dependencies, "dependencies should be a list")
        assertEquals(dependencies.size, 6, "There should have been 4 dependencies: $dependencies")
    }

}