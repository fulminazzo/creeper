package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.Downloader
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertThrows
import org.gradle.api.logging.Logging
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertTrue

class HttpPluginProviderIntegrationTest {
    private val provider = HttpPluginProvider(
        Logging.getLogger(HttpPluginProviderIntegrationTest::class.java),
        Downloader.http()
    )

    @Test
    fun `test that provider correctly downloads plugin`() {
        val destination = DIRECTORY.resolve(PLUGIN_NAME)
        destination.deleteIfExists()
        val request = HttpPluginRequest(RESOURCE_URL)
        provider.handleRequest(DIRECTORY, request)
        assertTrue(destination.exists(), "Downloaded plugin does not exist: $destination")
    }

    @Test
    fun `test that provider throws if plugin file could not be found`() {
        val request = HttpPluginRequest("https://github.com/fulminazzo/not-found")
        assertThrows<PluginNotFoundException> { provider.handleRequest(DIRECTORY, request) }
    }

    companion object {
        private val MAIN_DIRECTORY = Path.of("build/resources/integrationTest/provider/plugin")
        private const val RESOURCE_URL = "https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"
        private val DIRECTORY = MAIN_DIRECTORY.resolve("plugins")
        private const val PLUGIN_NAME = "YAGL-5.2.2.jar"

        @JvmStatic
        @BeforeAll
        fun setup() {
            DIRECTORY.createDirectories()
        }

    }

}