package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.cache.CacheManager
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GithubPluginProviderIntegrationTest {
    private val provider = GitHubPluginProvider(
        LoggerFactory.getLogger(GithubPluginProviderIntegrationTest::class.java),
        CachedDownloader.simple(Downloader.http())
    )

    @Test
    fun `test that provider correctly downloads plugin`() {
        val destination = DIRECTORY.resolve(RELEASE.name)
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
                GitHubPluginRequest("fulminazzo", "YAGL", "1.0.l", "YAGL-1.0.0.jar")
            )
        }
    }

    @Test
    fun `test that provider fetches correct metadata for release`() {
        val response = provider.fetchReleaseMetadata(REQUEST)
        assertEquals(RELEASE, response, "Response does not match expected release")
    }

    companion object {
        private val MAIN_DIRECTORY = Path.of("build/resources/integrationTest/provider/plugin")
        private val DIRECTORY = MAIN_DIRECTORY.resolve("plugins")

        private val REQUEST = GitHubPluginRequest(
            "fulminazzo",
            "YAGL",
            "5.2.2",
            "YAGL-5.2.2.jar"
        )
        private val RELEASE = Release(
            "https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar",
            "YAGL-5.2.2.jar",
            "sha256:6e720c6f62f6fa4c0e6f45a3ed85aa368a95ca37f1d44a97bdd36da960bc3721"
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