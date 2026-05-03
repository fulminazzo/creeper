package it.fulminazzo.creeper.provider.plugin

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.CompletionException
import kotlin.io.path.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LocalPluginProviderIntegrationTest {
    private val destination = DIRECTORY.resolve(PLUGIN_NAME)

    private val provider = LocalPluginProvider(
        LoggerFactory.getLogger(LocalPluginProviderIntegrationTest::class.java)
    ) { it.run() }

    @Test
    fun `test that provider correctly copies plugin`() {
        destination.deleteIfExists()

        val request = LocalPluginRequest(PLUGIN_FILE, true)

        provider.handleRequest(DIRECTORY, request).join()
        checkPluginFile()
    }

    @Test
    fun `test that provider overwrites plugin if requested`() {
        PLUGIN_FILE.copyTo(destination, overwrite = true)

        val request = LocalPluginRequest(PLUGIN_FILE, true)

        provider.handleRequest(DIRECTORY, request).join()
        checkPluginFile()
    }

    @Test
    fun `test that provider does not overwrite plugin if not requested`() {
        destination.writeText("Goodbye, mars!")

        val request = LocalPluginRequest(PLUGIN_FILE, false)

        provider.handleRequest(DIRECTORY, request).join()
        checkPluginFile()
        assertEquals("Goodbye, mars!", destination.readText(), "Plugin file was overwritten:")
    }

    @Test
    fun `test that provider throws if plugin file does not exist`() {
        val request = LocalPluginRequest(Path.of("not-found.jar"), true)

        val e = assertThrows<CompletionException> {
            provider.handleRequest(DIRECTORY, request).join()
        }
        assertIs<PluginNotFoundException>(e.cause)
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