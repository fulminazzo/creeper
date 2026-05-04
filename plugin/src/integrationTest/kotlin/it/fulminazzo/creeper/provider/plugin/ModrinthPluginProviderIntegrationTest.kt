package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.cache.CacheManager
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertThrows
import org.gradle.api.logging.Logging
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModrinthPluginProviderIntegrationTest {
    private val provider = ModrinthPluginProvider(
        Logging.getLogger(ModrinthPluginProviderIntegrationTest::class.java),
        CachedDownloader.simple(Downloader.http())
    )

    @Test
    fun `test that provider correctly downloads plugin`() {
        val destination = DIRECTORY.resolve(VERSION.name)
        DIRECTORY.toFile().deleteRecursively()
        val path = provider.handleRequest(DIRECTORY, REQUEST)
        assertTrue(destination.exists(), "Downloaded plugin does not exist: $destination")
        assertEquals(destination, path, "Downloaded path does not match expected path")
    }

    @Test
    fun `test that provider throws if the release could not be found`() {
        assertThrows<PluginNotFoundException> {
            provider.handleRequest(
                DIRECTORY,
                ModrinthPluginRequest("teleporteffects", "1.0.l", "TeleportEffects-3.0.0.jar")
            )
        }
    }

    @Test
    fun `test that provider fetches correct metadata for version`() {
        val response = provider.fetchVersionFileMetadata(REQUEST)
        assertEquals(VERSION, response, "Response does not match expected version file")
    }

    @Test
    fun `test that provider supports name of project along with slug`() {
        val request = ModrinthPluginRequest(
            "Simple Voice Chat",
            "bukkit-2.6.16",
            "voicechat-bukkit-2.6.16.jar"
        )
        val version = VersionFile(
            "5373ca217ab94ab0b54d2707a51eca46ddfad6f376d83361747086600362e46d3844d0288016f237c31c2666d2048b3d272117236e706d524a743a2e9047694c",
            "voicechat-bukkit-2.6.16.jar",
            "https://cdn.modrinth.com/data/9eGKb6K1/versions/ZQfVgh62/voicechat-bukkit-2.6.16.jar"
        )
        val response = provider.fetchVersionFileMetadata(request)
        assertEquals(version, response, "Response does not match expected version file")
    }

    companion object {
        private val MAIN_DIRECTORY = Path.of("build/resources/integrationTest/provider/plugin")
        private val DIRECTORY = MAIN_DIRECTORY.resolve("plugins")

        private val REQUEST = ModrinthPluginRequest(
            "teleporteffects",
            "3.0",
            "TeleportEffects-3.0.jar"
        )
        private val VERSION = VersionFile(
            "dfaae7a3f470d46a4f1556e234cddc8e1b67b515945dc07b712a99288cef2fb5f9f474be28c972a0f57d0133eb04998f4b2c378bfd709d43ccd52c40c36c528e",
            "TeleportEffects-3.0.jar",
            "https://cdn.modrinth.com/data/oyCFkeGb/versions/FkSIn5Fs/TeleportEffects-3.0.jar"
        )

        @JvmStatic
        @BeforeAll
        fun setup() {
            DIRECTORY.createDirectories()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            CacheManager.closeAll()
        }

    }

}