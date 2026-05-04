package it.fulminazzo.creeper.server.runner

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettingsBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.assertThrows
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

class MinecraftServerRunnerIntegrationTest {
    private val runner = MinecraftServerRunner(mockSpecification(), LOGGER, EXECUTOR, BASE_DIRECTORY, JAVA_EXECUTABLE)

    @Test
    fun `test normal start cycle`() {
        val specification = mockSpecification("MockServer-1.0", "-Xms1M")

        val runner = MinecraftServerRunner(specification, LOGGER, EXECUTOR, BASE_DIRECTORY, JAVA_EXECUTABLE)

        val pid = runner.start()
        runner.awaitCompleteBoot(10.seconds)
        runner.forceStop()
        runner.await()

        assertFalse(ProcessHandle.of(pid).isPresent, "Process $pid should have been stopped after forceStop")
    }

    @Test
    fun `test that execution error does not throw but is reported by wasSuccessfulExecution`() {
        val specification = mockSpecification("Exception-1.0")

        val runner = MinecraftServerRunner(specification, LOGGER, EXECUTOR, BASE_DIRECTORY, JAVA_EXECUTABLE)
        val pid = runner.start()
        runner.await()

        assertFalse(ProcessHandle.of(pid).isPresent, "Process $pid should have been stopped after error")
    }

    @Test
    fun `test that start throws if already running`() {
        val mock = spyk(runner)
        every { mock.isRunning() } returns true

        assertThrows<IllegalStateException> { mock.start() }
    }

    private fun mockSpecification(id: String, flags: String = ""): MinecraftServerSpec {
        val settings = mockk<MinecraftServerSettings>()
        every { settings.flags } returns flags
        every { settings.port } returns 10526

        val specification = mockk<MinecraftServerSpec>()
        every { specification.type } returns ServerType.VANILLA
        every { specification.version } returns "1.2.3"
        every { specification.id } returns id
        every { specification.settings } returns settings
        return specification
    }

    private companion object {
        private val LOGGER: Logger = Logging.getLogger(MinecraftServerRunnerIntegrationTest::class.java)
        private val EXECUTOR = Executors.newCachedThreadPool()
        private val BASE_DIRECTORY = Path.of("build/resources/integrationTest/server/runner")

        private val JAVA_EXECUTABLE = "${System.getProperty("java.home")}/bin/java"

        @JvmStatic
        @AfterAll
        fun tearDown() {
            EXECUTOR.close()
        }

        fun mockSpecification(): MinecraftServerSpec {
            val builder = MinecraftServerSettingsBuilder()
            builder.eula = true
            return MinecraftServerSpec(
                ServerType.VANILLA,
                "1.21.8",
                builder.build(),
                emptySet(),
                emptySet(),
                emptyList()
            )
        }

    }

}