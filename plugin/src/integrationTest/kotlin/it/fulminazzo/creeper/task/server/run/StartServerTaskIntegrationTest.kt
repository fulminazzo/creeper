package it.fulminazzo.creeper.task.server.run

import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.task.TaskIntegrationTestHelper
import it.fulminazzo.creeper.task.server.InstallServerRunnerTask
import java.io.File
import java.net.Socket
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StartServerTaskIntegrationTest : TaskIntegrationTestHelper() {

    @Test
    fun `test that StartServerTask creates a new detached process and updates status file`() {
        val specification = MinecraftServerSpec(
            type = ServerType.PAPER,
            version = "1.8.8"
        )

        val port = 18526

        val task = createTask(StartServerTask::class.java) { task ->
            task.specification.set(specification)
            task.port.set(port)
            task.runnerJar.set(RUNNER_JAR)
            task.statusFile.set(STATUS_FILE)
        }

        RunTaskUtils.copyRunFilesToTaskWorkDir(
            WORK_DIR,
            task.statusFile.get().asFile.parentFile,
            specification
        )

        val statusFile = task.statusFile.get().asFile

        task.run()

        assertTrue(statusFile.exists(), "The status file should have been created")

        val data = CreeperPlugin.PROPERTIES_MAPPER.readValue<Map<String, Any>>(statusFile)

        val pid = data["pid"]
        assertNotNull(pid, "The PID should have been set in the status file")
        assertEquals(
            port.toString(),
            data["port"],
            "The port should have been set in the status file"
        )

        val optProcessHandle = ProcessHandle.of(pid.toString().toLong())
        assertTrue(optProcessHandle.isPresent, "The process should be present")
        val processHandle = optProcessHandle.get()
        assertTrue(processHandle.isAlive, "The process should be alive")

        val client = Socket("0.0.0.0", port)
        val output = client.outputStream
        output.write("stopprocess\n".toByteArray())
        output.flush()
        client.close()
    }

    private companion object {
        private val WORK_DIR = File("build/resources/integrationTest/task/server/run")

        private val RUNNER_JAR = File(WORK_DIR, "server-runner.jar")

        private val STATUS_FILE = File(WORK_DIR, "${ProjectInfo.NAME}.properties")

    }

}