package it.fulminazzo.creeper.task.server.start

import it.fulminazzo.creeper.PROPERTIES_MAPPER
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.task.TaskIntegrationTestHelper
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class StopServerTaskIntegrationTest : TaskIntegrationTestHelper() {

    @Test
    fun `test that StopServerTask correctly stops running server`() {
        val specification = MinecraftServerSpec(
            type = ServerType.PAPER,
            version = "1.8.8"
        )

        val port = 18527

        val requestedStopFile = File(WORK_DIR, "request-stop")
        val task = createTask(StopServerTask::class.java) { task ->
            task.statusFile.set(STATUS_FILE)
            task.awaitTimeout.set(5L)
            task.requestedStopFile.set(requestedStopFile)
        }

        RunTaskUtils.copyRunFilesToTaskWorkDir(
            WORK_DIR,
            task.statusFile.get().asFile.parentFile
        )

        val statusFile = task.statusFile.get().asFile

        val workDir = statusFile.parentFile
        val process = ProcessBuilder(
            "java", "-jar", "server-runner.jar",
            port.toString(),
            File(workDir, specification.id).absolutePath,
            "java", "-jar",
            "${specification.id}.jar",
            "nogui"
        ).directory(workDir).redirectErrorStream(true).start()
        Thread.sleep(1_000L)

        PROPERTIES_MAPPER.writeValue(
            statusFile, mapOf(
                "pid" to process.pid(),
                "port" to port
            )
        )

        task.run()
        Thread.sleep(2_000L)

        assertNotNull(process.exitValue(), "The server process should have exited")
        assertFalse(process.isAlive, "The server process should have been stopped")

        assertFalse(
            project.file(requestedStopFile).exists(),
            "The requested stop file should have been deleted"
        )
    }

    private companion object {
        private val WORK_DIR = File("build/resources/integrationTest/task/server/start")

        private val STATUS_FILE = File(WORK_DIR, "${ProjectInfo.NAME}.properties")

    }

}