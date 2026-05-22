package it.fulminazzo.creeper.task.server.start

import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.PROPERTIES_MAPPER
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerConnector
import it.fulminazzo.creeper.task.server.InstallServerRunnerTask
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.io.IOException
import java.net.Socket
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StartServerTaskRegistrarFunctionalTest {
    private val projectDir = File("build/resources/functionalTest/task/server/start/server")

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

        val server = RESOURCE_BUILD_FILE.parent.resolve("paper-1.21").toFile()
        val serverDir = projectDir.resolve("paper-1.21")
        server.copyRecursively(serverDir, overwrite = true)

        val serverRunner = InstallServerRunnerTask.FILE_PATH.toFile()
        serverRunner.copyTo(serverDir.resolve("server-runner.jar"), overwrite = true)
    }

    @Test
    fun `test that stop task does not throw if server already stopped`() {
        runner.withArguments("startPaper1_21").build()

        val serverDir = projectDir.resolve("paper-1.21")
        assertTrue(serverDir.exists(), "Server directory does not exist on first run: $serverDir")

        val (statusFile, pid, tcpPort) = testSuccessfulStart(serverDir)

        val connector = ServerConnector(tcpPort)
        connector.connect()
        assertTrue(connector.connected, "TCP Server should be connected")
        connector.send("stopprocess")
        connector.disconnect()
        assertFalse(connector.connected, "TCP Server should not be connected")

        Thread.sleep(WAIT_STOP_SECONDS * 1000)
        runner.withArguments("stopPaper1_21").build()

        testSuccessfulStop(tcpPort, pid, statusFile)
    }

    @Test
    fun `test that start task does not run again if already running`() {
        runner.withArguments("startPaper1_21").build()

        val serverDir = projectDir.resolve("paper-1.21")
        assertTrue(serverDir.exists(), "Server directory does not exist on first run: $serverDir")

        val statusFile = serverDir.resolve("${ProjectInfo.NAME}-status.properties")
        assertTrue(statusFile.exists(), "Status file ${statusFile.path} does not exist on first run")

        val firstData = PROPERTIES_MAPPER.readValue<Map<String, Any>>(statusFile)

        runner.withArguments("startPaper1_21").build()

        assertTrue(statusFile.exists(), "Status file ${statusFile.path} does not exist on second run")

        val secondData = PROPERTIES_MAPPER.readValue<Map<String, Any>>(statusFile)

        assertEquals(
            firstData,
            secondData,
            "Status file should not have been modified"
        )
    }

    @Test
    fun `test start stop cycle`() {
        runner.withArguments("startPaper1_21").build()

        val serverDir = projectDir.resolve("paper-1.21")
        assertTrue(serverDir.exists(), "Server directory does not exist: $serverDir")

        val (statusFile, pid, tcpPort) = testSuccessfulStart(serverDir)

        val minecraftPort = 25566
        assertTrue(
            isServerRunning(minecraftPort),
            "Minecraft Server is not running on port $minecraftPort"
        )

        runner.withArguments("stopPaper1_21").build()
        testSuccessfulStop(tcpPort, pid, statusFile)
    }

    private fun testSuccessfulStart(serverDir: File): Triple<File, Long, Int> {
        val statusFile = serverDir.resolve("${ProjectInfo.NAME}-status.properties")
        assertTrue(statusFile.exists(), "Status file ${statusFile.path} does not exist")

        val data = PROPERTIES_MAPPER.readValue<Map<String, Any>>(statusFile)
        val pid = data["pid"]?.toString()?.toLong()
        assertNotNull(pid, "PID should not be null")
        assertTrue(
            ProcessHandle.of(pid).isPresent,
            "Could not find process from PID $pid"
        )

        val tcpPort = data["port"]?.toString()?.toInt()
        assertNotNull(tcpPort, "TCP port should not be null")
        assertTrue(
            isServerRunning(tcpPort),
            "TCP Server is not running on port $tcpPort"
        )
        return Triple(statusFile, pid, tcpPort)
    }

    private fun testSuccessfulStop(tcpPort: Int, pid: Long, statusFile: File) {
        val minecraftPort = 25565
        Thread.sleep(WAIT_STOP_SECONDS * 1000)
        assertFalse(
            isServerRunning(minecraftPort),
            "Minecraft Server is still running on port $minecraftPort"
        )
        assertFalse(
            isServerRunning(tcpPort),
            "TCP Server is still running on port $tcpPort"
        )
        assertFalse(
            ProcessHandle.of(pid).isPresent,
            "Process with PID $pid should be closed by now"
        )
        assertFalse(
            statusFile.exists(),
            "status file should not exist"
        )
    }

    private companion object {
        private const val WAIT_STOP_SECONDS = 5L

        private val RESOURCE_BUILD_FILE = Path.of("src/functionalTest/resources/task/server/start/build.gradle.kts")

        private fun isServerRunning(port: Int): Boolean {
            return try {
                Socket("127.0.0.1", port).close()
                true
            } catch (_: IOException) {
                false
            }
        }

    }

}