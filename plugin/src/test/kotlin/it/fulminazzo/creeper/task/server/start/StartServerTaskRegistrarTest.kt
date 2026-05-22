package it.fulminazzo.creeper.task.server.start

import io.mockk.every
import io.mockk.mockk
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.task.server.RegistrarTestHelper
import org.gradle.api.Task
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class StartServerTaskRegistrarTest : RegistrarTestHelper() {

    @Test
    fun `test that registerStartTask correctly registers task`() {
        StartServerTaskRegistrar.register(
            project,
            specification, 
            SERVER_DIRECTORY.resolve("server-runner.jar"),
            SERVER_DIRECTORY
        )

        val (_, checkServerTask) = testTaskMetadata<CheckServerStatusTask>("checkServer${taskBaseName}", false)
        val (_, checkServerStopTask) = testTaskMetadata<Task>("checkStopRequired${taskBaseName}ForCheckServer", false)

        val (_, runServerTask) = testTaskMetadata<RunServerTask>("run${taskBaseName}", false)

        val (_, awaitServerTask) = testTaskMetadata<AwaitBootCompleteTask>("awaitBootComplete${taskBaseName}", false)
        val (_, awaitServerStopTask) = testTaskMetadata<Task>("checkStopRequired${taskBaseName}ForAwaitBootComplete", false)

        testTaskMetadata<StopServerTask>("stop${taskBaseName}", true)
        val (_, startServerTask) = testTaskMetadata<Task>("start${taskBaseName}", true)

        testDependency(checkServerStopTask, checkServerTask)

        testDependency(runServerTask, checkServerStopTask)

        testDependency(awaitServerTask, runServerTask)
        testDependency(awaitServerStopTask, awaitServerTask)

        testDependency(startServerTask, awaitServerStopTask)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerCheckServerTask correctly registers task`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers)
        registrar.registerCheckServerTask()

        val (taskName, task) = testTaskMetadata<CheckServerStatusTask>("checkServer$taskBaseName", showWorkers)

        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )

        testStatusFile(taskName, task.statusFile.orNull?.asFile)
        testRequestedStartFile(taskName, task.requestedStartFile.orNull?.asFile)
        testRequestedStopFile(taskName, task.requestedStopFile.orNull?.asFile)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test thatRegisterRunTask correctly registers task`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers)
        registrar.registerRunTask()

        val (taskName, task) = testTaskMetadata<RunServerTask>("run$taskBaseName", showWorkers)

        testRequestedStartFile(taskName, task.requestedStartFile.orNull?.asFile)

        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )

        assertEquals(
            StartServerTaskRegistrar.DEFAULT_TCP_SERVER_PORT,
            task.port.orNull,
            "Task $taskName should have port ${StartServerTaskRegistrar.DEFAULT_TCP_SERVER_PORT}"
        )

        val runnerJar = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve("server-runner.jar")
            .toFile()
        assertEquals(
            runnerJar,
            task.runnerJar.orNull?.asFile,
            "Task $taskName should have runnerJar pointing to $runnerJar"
        )

        testStatusFile(taskName, task.statusFile.orNull?.asFile)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerAwaitBootCompleteTask correctly registers task`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers)
        registrar.registerAwaitBootCompleteTask()

        val (taskName, task) = testTaskMetadata<AwaitBootCompleteTask>("awaitBootComplete$taskBaseName", showWorkers)

        val logFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve("logs/latest.log")
            .toFile()
        assertEquals(
            logFile,
            task.logFile.orNull?.asFile,
            "Task $taskName should have logFile pointing to $logFile"
        )

        assertEquals(
            StartServerTaskRegistrar.DEFAULT_BOOT_AWAIT_TIMEOUT,
            task.awaitTimeout.orNull,
            "Task $taskName should have awaitTimeout ${StartServerTaskRegistrar.DEFAULT_BOOT_AWAIT_TIMEOUT}"
        )

        testStatusFile(taskName, task.statusFile.orNull?.asFile)

        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )

        testRequestedStopFile(taskName, task.requestedStopFile.orNull?.asFile)
    }

    @Test
    fun `test that registerStopTask correctly registers task`() {
        val registrar = createRegistrar()
        registrar.registerStopTask()

        val (taskName, task) = testTaskMetadata<StopServerTask>("stop$taskBaseName", true)

        assertEquals(
            StartServerTaskRegistrar.DEFAULT_STOP_TIMEOUT,
            task.awaitTimeout.orNull,
            "Task $taskName should have awaitTimeout ${StartServerTaskRegistrar.DEFAULT_STOP_TIMEOUT}"
        )

        testStatusFile(taskName, task.statusFile.orNull?.asFile)
        testRequestedStopFile(taskName, task.requestedStopFile.orNull?.asFile)
    }

    private fun testStatusFile(taskName: String, file: File?) {
        val statusFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(StartServerTaskRegistrar.STATUS_FILE_NAME)
            .toFile()
        assertEquals(
            statusFile,
            file,
            "Task $taskName should have statusFile pointing to $statusFile"
        )
    }

    private fun testRequestedStartFile(taskName: String, file: File?) {
        val requestedStartFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(StartServerTaskRegistrar.REQUESTED_START_FILENAME)
            .toFile()
        assertEquals(
            requestedStartFile,
            file,
            "Task $taskName should have requestedStartFile pointing to $requestedStartFile"
        )
    }

    private fun testRequestedStopFile(taskName: String, file: File?) {
        val requestedStopFile = project.projectDir.toPath()
            .resolve(SERVER_DIRECTORY)
            .resolve(StartServerTaskRegistrar.REQUESTED_STOP_FILENAME)
            .toFile()
        assertEquals(
            requestedStopFile,
            file,
            "Task $taskName should have requestedStopFile pointing to $requestedStopFile"
        )
    }

    private fun createRegistrar(showWorkers: Boolean = false): StartServerTaskRegistrar {
        val registrar = StartServerTaskRegistrar(
            specification,
            SERVER_DIRECTORY.resolve("server-runner.jar"),
            showWorkers
        )
        registrar.project = project
        registrar.serverDirectory = SERVER_DIRECTORY
        registrar.statusFile = SERVER_DIRECTORY.resolve(StartServerTaskRegistrar.STATUS_FILE_NAME)
        registrar.requestedStartFile = SERVER_DIRECTORY.resolve(StartServerTaskRegistrar.REQUESTED_START_FILENAME)
        registrar.requestedStopFile = SERVER_DIRECTORY.resolve(StartServerTaskRegistrar.REQUESTED_STOP_FILENAME)
        return registrar
    }

}