package it.fulminazzo.creeper.task.server.run

import it.fulminazzo.creeper.task.server.RegistrarTestHelper
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class RunServerTaskRegistrarTest : RegistrarTestHelper() {

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerStopServerTask correctly registers task`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerStopServerTask()

        val (taskName, task) = testTaskMetadata<StopServerTask>(
            "stopServer$taskBaseName",
            showWorkers
        )

        val stopFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(RunServerTaskRegistrar.STOP_REQUIRED_FILE_NAME)
        assertEquals(
            stopFile.toFile(),
            task.stopRequiredFile.orNull?.asFile,
            "Task $taskName should have stopRequiredFile pointing to $stopFile"
        )
        assertEquals(
            RunServerTaskRegistrar.DEFAULT_STOP_TIMEOUT,
            task.awaitTimeout.orNull,
            "Task $taskName should have awaitTimeout ${RunServerTaskRegistrar.DEFAULT_STOP_TIMEOUT} seconds"
        )
        val statusFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(RunServerTaskRegistrar.STATUS_FILE_NAME)
        assertEquals(
            statusFile.toFile(),
            task.statusFile.orNull?.asFile,
            "Task $taskName should have statusFile pointing to $statusFile"
        )
    }

    private fun createRegistrar(
        setupStopServerTask: Boolean = true,
        showWorkers: Boolean = false
    ): RunServerTaskRegistrar {
        val registrar = RunServerTaskRegistrar(
            specification,
            SERVER_DIRECTORY.resolve("server-runner.jar"),
            showWorkers
        )
        registrar.project = project
        registrar.serverDirectory = SERVER_DIRECTORY
        registrar.statusFile = SERVER_DIRECTORY.resolve(RunServerTaskRegistrar.STATUS_FILE_NAME)
        registrar.stopRequiredFile = SERVER_DIRECTORY.resolve(RunServerTaskRegistrar.STOP_REQUIRED_FILE_NAME)
        if (setupStopServerTask) registrar.stopServerTask = setupTask()
        return registrar
    }

}