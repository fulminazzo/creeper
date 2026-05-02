package it.fulminazzo.creeper.server.runner

import io.mockk.*
import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.MinecraftServerSpec
import it.fulminazzo.creeper.server.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.server.spec.settings.MinecraftServerSettingsBuilder
import it.fulminazzo.creeper.util.VersionUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.assertThrows
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

class MinecraftServerRunnerIntegrationTest {
    private val runner = MinecraftServerRunner(mockSpecification(), LOGGER, EXECUTOR, BASE_DIRECTORY)

    @Test
    fun `test normal start cycle`() {
        val specification = mockSpecification("MockServer-1.0", "-Xms1M")

        val runner = MinecraftServerRunner(specification, LOGGER, EXECUTOR, BASE_DIRECTORY)

        val pid = runner.start()

        runner.awaitCompleteBoot(10.seconds).join()

        runner.forceStop()

        Thread.sleep(500)

        assertFalse(ProcessHandle.of(pid).isPresent, "Process $pid should have been stopped after forceStop")
    }

    @Test
    fun `test that execution error does not throw but is reported by wasSuccessfulExecution`() {
        val specification = mockSpecification("Exception-1.0")

        val runner = MinecraftServerRunner(specification, LOGGER, EXECUTOR, BASE_DIRECTORY)

        val pid = runner.start()

        Thread.sleep(500)

        assertFalse(ProcessHandle.of(pid).isPresent, "Process $pid should have been stopped after error")
        assertFalse(runner.wasExecutionSuccessful(), "Execution should not have been successful after error")
    }

    @Test
    fun `test that start throws if already running`() {
        val mock = spyk(runner)
        every { mock.isRunning() } returns true

        assertThrows<IllegalStateException> { mock.start() }
    }

    @Test
    fun `test that runner throws if current Java version does not support required`() {
        mockkObject(VersionUtils)

        every { VersionUtils.getJavaVersion(any()) } returns Runtime.Version.parse("26")

        assertThrows<IllegalStateException> { runner.checkJavaVersion() }

        unmockkObject(VersionUtils)
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
        private val LOGGER: Logger = LoggerFactory.getLogger(MinecraftServerRunnerIntegrationTest::class.java)
        private val EXECUTOR = Executors.newCachedThreadPool()
        private val BASE_DIRECTORY = Path.of("build/resources/integrationTest/server/runner")

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