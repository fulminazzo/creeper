package it.fulminazzo.creeper.provider

import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.util.HttpUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.gradle.api.logging.Logging
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.test.Test

class MCJarsApiProviderIntegrationTest {
    private val downloader = CachedDownloader.global(Downloader.http())

    private val provider = MCJarsApiProvider(
        downloader,
        Logging.getLogger(MCJarsApiProviderIntegrationTest::class.java)
    )

    @Test
    fun `test that MinecraftJarProvider#get works`() {
        val destination = WORK_DIR.resolve("${PLATFORM.id}-$VERSION.jar")
        destination.deleteIfExists()

        provider.get(PLATFORM, VERSION, destination.parent)
        Assertions.assertTrue(destination.exists(), "JAR file does not exist: ${destination.toAbsolutePath()}")
    }

    @Test
    fun `test that MinecraftJarProvider#get throws if jar is not found`() {
        val version = "1.8.8-not-found"
        val destination = WORK_DIR.resolve("${PLATFORM.id}-$version.jar")

        assertThrows<JarNotFoundException> { provider.get(PLATFORM, version, destination.parent) }
    }

    @Test
    fun `test that MinecraftConfigProvider#get works`() {
        val destination = WORK_DIR.resolve("server.properties")
        destination.deleteIfExists()

        provider.get(destination.name, PLATFORM, VERSION, destination.parent)
        Assertions.assertTrue(
            destination.exists(),
            "configuration file does not exist: ${destination.toAbsolutePath()}"
        )
    }

    @Test
    fun `test that MinecraftConfigProvider#get throws if configuration is not found`() {
        val version = "1.8.8-not-found"
        val destination = WORK_DIR.resolve("server.properties")

        assertThrows<ConfigurationNotFoundException> {
            provider.get(destination.name, PLATFORM, version, destination.parent)
        }
    }

    @Test
    fun `test fetchBuild internal cache`() {
        mockkObject(HttpUtils)

        var actual = provider.fetchBuild(PLATFORM, VERSION)
        Assertions.assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
        verify(exactly = 1) { HttpUtils.getApi(any()) }

        actual = provider.fetchBuild(PLATFORM, VERSION)
        Assertions.assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
        verify(exactly = 1) { HttpUtils.getApi(any()) }

        unmockkObject(HttpUtils)
    }

    @Test
    fun `test that fetchBuild returns correct data`() {
        val actual = provider.fetchBuild(PLATFORM, VERSION)
        Assertions.assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
    }

    @Test
    fun `test that fetchConfig returns correct data`() {
        val expectedConfig = EXPECTED_CONFIG
        val actual = provider.fetchConfig(expectedConfig.name, PLATFORM, VERSION)
        Assertions.assertEquals(expectedConfig, actual, "config data was not equal")
    }

    companion object {
        private val WORK_DIR = Path.of("build/resources/integrationTest/provider/mcjars_api_provider_test")

        private val PLATFORM = ServerType.VANILLA
        private const val VERSION = "1.8.8"

        private val EXPECTED_BUILD_RESPONSE = BuildResponse(
            UUID.fromString("4e36bf73-fa69-4bd1-b86a-24de658ae2e6"),
            8322852L,
            "https://launcher.mojang.com/v1/objects/5fafba3f58c40dc51b5c3ca72a98f62dfdae1db7/server.jar"
        )
        private val EXPECTED_CONFIG = Config(
            UUID.fromString("ce8ba7dd-71fd-49ba-b31e-9466033e0ef4"),
            "server.properties",
            Path.of("build/resources/integrationTest/provider/server.properties")
                .readLines()
                .joinToString("\n")
        )

    }

}