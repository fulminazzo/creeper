package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GithubPluginProviderIntegrationTest {
    private val provider = GitHubPluginProvider(
        DIRECTORY,
        LoggerFactory.getLogger(GithubPluginProviderIntegrationTest::class.java),
        CachedDownloader.simple(Downloader.http())
    )

    @Test
    fun `test that provider fetches correct metadata for release`() {
        val response = provider.fetchReleaseMetadata(REQUEST).join()
        assertEquals(RELEASE, response, "Response does not match expected release")
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that updateCache creates and updates cache file regardless of existence`(exists: Boolean) {
        val cacheFile = GitHubPluginProvider.CACHE_FILE
        cacheFile.deleteIfExists()
        if (exists) {
            cacheFile.parent.createDirectories()
            cacheFile.writeText("{}")
        }

        GitHubPluginProvider.updateCache(REQUEST, RELEASE)
        assertTrue(cacheFile.exists(), "Cache file does not exist: $cacheFile")

        val data = JSON_MAPPER.readValue<Map<String, ReleaseCache>>(cacheFile.toFile())
        val key = REQUEST.toHashString()
        assertContains(data, key, "Cache file does not contain request hash:")
        val cache = data[key]!!
        assertEquals(cache, GitHubPluginProvider.getCachedRelease(REQUEST), "Cache file does not contain cached release:")
        assertEquals(RELEASE, cache.release, "Cache file does not contain release data:")
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

        private val JSON_MAPPER = jacksonObjectMapper()

        @JvmStatic
        @BeforeAll
        fun setup() {
            DIRECTORY.createDirectories()
        }

    }

}