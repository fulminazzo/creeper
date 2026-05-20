package it.fulminazzo.creeper.task.test.runner

import it.fulminazzo.creeper.ProjectInfo
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

class SetupTestRunnerRegistrarFunctionalTest {
    private val projectDir = File("build/resources/functionalTest/task/test/runner/setup")

    private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }

    private val runner = GradleRunner.create()
        .forwardOutput()
        .withPluginClasspath()
        .withProjectDir(projectDir)

    @BeforeEach
    fun setup() {
        projectDir.deleteRecursively()
        projectDir.mkdirs()
        settingsFile.writeText("")
        buildFile.writeText(RESOURCE_BUILD_FILE.toFile().readText())
    }

    @Test
    fun `test that buildTestRunner task correctly builds test runner`() {
        runner.withArguments("buildTestRunner").build()

        val runnerJar = projectDir.resolve("build/${ProjectInfo.NAME}/repository/base/build/libs/${ProjectInfo.NAME}-${ProjectInfo.VERSION}.jar")
        assertTrue(runnerJar.exists(), "Runner jar does not exist: $runnerJar")
    }

    private companion object {
        private val RESOURCE_BUILD_FILE = Path.of("src/functionalTest/resources/task/test/runner/build.gradle.kts")

    }

}
