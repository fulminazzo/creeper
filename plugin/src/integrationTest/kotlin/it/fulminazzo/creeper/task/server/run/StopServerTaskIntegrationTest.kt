package it.fulminazzo.creeper.task.server.run

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.task.TaskIntegrationTestHelper
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse

class StopServerTaskIntegrationTest : TaskIntegrationTestHelper() {

    @Test
    fun `test that StopServerTask correctly stops running server`() {
        val specification = MinecraftServerSpec(
            type = ServerType.PAPER,
            version = "1.8.8"
        )

        val port = 18527

        val task = createTask(StopServerTask::class.java) { task ->
            task.statusFile.set(STATUS_FILE)
        }

        RunTaskUtils.copyRunFilesToTaskWorkDir(
            WORK_DIR,
            task.statusFile.get().asFile.parentFile,
            specification
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

        CreeperPlugin.PROPERTIES_MAPPER.writeValue(
            statusFile, mapOf(
                "pid" to process.pid(),
                "port" to port
            )
        )

        task.run()

        assertFalse(process.isAlive, "The server process should have been stopped")
    }

    private companion object {
        private val WORK_DIR = File("build/resources/integrationTest/task/server/run")

        private val STATUS_FILE = File(WORK_DIR, "${ProjectInfo.NAME}.properties")

    }

}