package it.fulminazzo.creeper.task.server.run

import it.fulminazzo.creeper.task.server.RegistrarTestHelper
import org.gradle.api.Task
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertContains
import kotlin.test.assertEquals

class RunServerTaskRegistrarTest : RegistrarTestHelper() {

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerStartServerTask correctly registers task`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerStartServerTask()

        val (taskName, task) = testTaskMetadata<StartServerTask>(
            "startServer$taskBaseName",
            showWorkers
        )

        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have specification $specification"
        )
        assertEquals(
            RunServerTaskRegistrar.DEFAULT_TCP_SERVER_PORT,
            task.port.orNull,
            "Task $taskName should have port ${RunServerTaskRegistrar.DEFAULT_TCP_SERVER_PORT}"
        )
        val runnerExecutable = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve("server-runner.jar")
        assertEquals(
            runnerExecutable.toFile(),
            task.runnerJar.orNull?.asFile,
            "Task $taskName should have runnerJar pointing to $runnerExecutable"
        )
        val statusFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(RunServerTaskRegistrar.STATUS_FILE_NAME)
        assertEquals(
            statusFile.toFile(),
            task.statusFile.orNull?.asFile,
            "Task $taskName should have statusFile pointing to $statusFile"
        )

        testFinalizedByStopServerTask(registrar, task)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerAwaitBootCompleteTask correctly registers task`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerAwaitBootCompleteTask()

        val (taskName, task) = testTaskMetadata<AwaitBootCompleteTask>(
            "awaitBootComplete$taskBaseName",
            showWorkers
        )

        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have specification $specification"
        )
        val statusFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(RunServerTaskRegistrar.STATUS_FILE_NAME)
        assertEquals(
            statusFile.toFile(),
            task.statusFile.orNull?.asFile,
            "Task $taskName should have statusFile pointing to $statusFile"
        )
        val logFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve("logs/latest.log")
        assertEquals(
            logFile.toFile(),
            task.logFile.orNull?.asFile,
            "Task $taskName should have logFile pointing to $logFile"
        )
        assertEquals(
            RunServerTaskRegistrar.DEFAULT_BOOT_AWAIT_TIMEOUT,
            task.awaitTimeout.orNull,
            "Task $taskName should have awaitTimeout ${RunServerTaskRegistrar.DEFAULT_BOOT_AWAIT_TIMEOUT} seconds"
        )
        val stopFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(RunServerTaskRegistrar.STOP_REQUIRED_FILE_NAME)
        assertEquals(
            stopFile.toFile(),
            task.stopRequiredFile.orNull?.asFile,
            "Task $taskName should have stopRequiredFile pointing to $stopFile"
        )

        testFinalizedByStopServerTask(registrar, task)
    }

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

    private fun testFinalizedByStopServerTask(registrar: RunServerTaskRegistrar, task: Task) {
        val stopServerTask = registrar.stopServerTask
        assertContains(
            task.finalizedBy.getDependencies(task),
            stopServerTask,
            "Task ${task.name} should be finalized by ${stopServerTask.name}"
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