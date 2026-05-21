package it.fulminazzo.creeper.task.server

import it.fulminazzo.creeper.task.TaskTestHelper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.Ignore

@Ignore //TODO: fix
class ExtractServerRunnerTaskTest : TaskTestHelper() {

    @Test
    fun `test that ExtractServerRunnerTask works`() {
        val runner = File("build/resources/test/tmp/server-runner.jar").absoluteFile
        runner.delete()

        val task = createTask(ExtractServerRunnerTask::class.java) { task ->
            task.runnerJar.set(runner)
        }

        task.run()

        assertTrue(runner.exists(), "The server runner JAR should have been extracted")
    }

}