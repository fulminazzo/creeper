package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.cache.CacheManager
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.gradle.api.logging.Logging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedirectPluginProviderIntegrationTest {
    private val provider = RedirectPluginProvider(
        Logging.getLogger(ModrinthPluginProviderIntegrationTest::class.java),
        CachedDownloader.simple(Downloader.http())
    )

    @Test
    fun `test that provider correctly downloads Modrinth plugin`() {
        val destination = DIRECTORY.resolve(MODRINTH_VERSION.name)
        DIRECTORY.toFile().deleteRecursively()
        val path = provider.handleRequest(DIRECTORY, MODRINTH_REQUEST)
        assertTrue(destination.exists(), "Downloaded plugin does not exist: $destination")
        assertEquals(destination, path, "Downloaded path does not match expected path")
    }

    @Test
    fun `test that provider correctly downloads Github plugin`() {
        val destination = DIRECTORY.resolve(GITHUB_RELEASE.name)
        DIRECTORY.toFile().deleteRecursively()
        val path = provider.handleRequest(
            DIRECTORY,
            GITHUB_REQUEST
        )
        assertTrue(destination.exists(), "Downloaded plugin does not exist: $destination")
        assertEquals(destination, path, "Downloaded path does not match expected path")
    }

    @Test
    fun `test that provider correctly downloads Http plugin`() {
        val destination = DIRECTORY.resolve(HTTP_PLUGIN_NAME)
        destination.deleteIfExists()
        val request = HttpPluginRequest(HTTP_RESOURCE_URL)
        provider.handleRequest(DIRECTORY, request)
        assertTrue(destination.exists(), "Downloaded plugin does not exist: $destination")
    }

    @Test
    fun `test that provider correctly copies local plugin`() {
        val destination = DIRECTORY.resolve(LOCAL_PLUGIN_NAME)
        destination.deleteIfExists()

        val request = LocalPluginRequest(LOCAL_PLUGIN_FILE, true)

        provider.handleRequest(DIRECTORY, request)
        assertTrue(destination.exists(), "Plugin file does not exist: ${destination.toAbsolutePath()}")
    }

    private companion object {
        private val MAIN_DIRECTORY = Path.of("build/resources/integrationTest/provider/plugin")
        private val DIRECTORY = MAIN_DIRECTORY.resolve("plugins")

        // MODRINTH
        private val MODRINTH_REQUEST = ModrinthPluginRequest(
            "teleporteffects",
            "3.0",
            "TeleportEffects-3.0.jar"
        )
        private val MODRINTH_VERSION = VersionFile(
            "dfaae7a3f470d46a4f1556e234cddc8e1b67b515945dc07b712a99288cef2fb5f9f474be28c972a0f57d0133eb04998f4b2c378bfd709d43ccd52c40c36c528e",
            "TeleportEffects-3.0.jar",
            "https://cdn.modrinth.com/data/oyCFkeGb/versions/FkSIn5Fs/TeleportEffects-3.0.jar"
        )

        // GITHUB
        private val GITHUB_REQUEST = GitHubPluginRequest(
            "fulminazzo",
            "YAGL",
            "5.2.2",
            "YAGL-5.2.2.jar"
        )
        private val GITHUB_RELEASE = Release(
            "https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar",
            "YAGL-5.2.2.jar",
            "sha256:6e720c6f62f6fa4c0e6f45a3ed85aa368a95ca37f1d44a97bdd36da960bc3721"
        )

        // HTTP
        private const val HTTP_PLUGIN_NAME = "YAGL-5.2.2.jar"
        private const val HTTP_RESOURCE_URL =
            "https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"

        // LOCAL
        private const val LOCAL_PLUGIN_NAME = "test.jar"
        private val LOCAL_PLUGIN_FILE = MAIN_DIRECTORY.resolve(LOCAL_PLUGIN_NAME)

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