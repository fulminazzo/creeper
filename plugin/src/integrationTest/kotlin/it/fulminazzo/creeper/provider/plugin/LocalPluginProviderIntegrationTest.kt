package it.fulminazzo.creeper.provider.plugin

import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalPluginProviderIntegrationTest {
    private val destination = DIRECTORY.resolve(PLUGIN_NAME)

    private val provider = LocalPluginProvider(
        DIRECTORY,
        LoggerFactory.getLogger(LocalPluginProviderIntegrationTest::class.java)
    )

    @Test
    fun `test that provider correctly copies plugin`() {
        destination.deleteIfExists()

        val request = LocalPluginRequest(PLUGIN_FILE, true)

        provider.handleRequest(request).join()
        checkPluginFile()
    }

    @Test
    fun `test that provider overwrites plugin if requested`() {
        PLUGIN_FILE.copyTo(destination, overwrite = true)

        val request = LocalPluginRequest(PLUGIN_FILE, true)

        provider.handleRequest(request).join()
        checkPluginFile()
    }

    @Test
    fun `test that provider does not overwrite plugin if not requested`() {
        destination.writeText("Goodbye, mars!")

        val request = LocalPluginRequest(PLUGIN_FILE, false)

        provider.handleRequest(request).join()
        checkPluginFile()
        assertEquals("Goodbye, mars!", destination.readText(), "Plugin file was overwritten:")
    }

    private fun checkPluginFile() {
        assertTrue(destination.exists(), "Plugin file does not exist: ${destination.toAbsolutePath()}")
    }

    companion object {
        private val MAIN_DIRECTORY = Path.of("build/resources/integrationTest/provider/plugin")
        private const val PLUGIN_NAME = "test.jar"
        private val PLUGIN_FILE = MAIN_DIRECTORY.resolve(PLUGIN_NAME)
        private val DIRECTORY = MAIN_DIRECTORY.resolve("plugins")

        @JvmStatic
        @BeforeAll
        fun setup() {
            DIRECTORY.createDirectories()
        }

    }

}