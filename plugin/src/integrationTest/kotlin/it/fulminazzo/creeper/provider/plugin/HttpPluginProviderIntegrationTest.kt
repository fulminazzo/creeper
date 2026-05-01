package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.Downloader
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertTrue

class HttpPluginProviderIntegrationTest {
    private val provider = HttpPluginProvider(
        DIRECTORY,
        LoggerFactory.getLogger(HttpPluginProviderIntegrationTest::class.java),
        { it.run() },
        Downloader.http()
    )

    @Test
    fun `test that provider correctly downloads plugin`() {
        val destination = DIRECTORY.resolve(PLUGIN_NAME)
        destination.deleteIfExists()
        val request = HttpPluginRequest(RESOURCE_URL)
        provider.handleRequest(request).join()
        assertTrue(destination.exists(), "Downloaded plugin does not exist: $destination")
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