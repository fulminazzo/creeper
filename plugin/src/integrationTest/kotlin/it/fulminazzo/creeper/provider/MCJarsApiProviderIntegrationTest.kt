package it.fulminazzo.creeper.provider

import io.mockk.spyk
import io.mockk.verify
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import it.fulminazzo.creeper.server.ServerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.test.Test
import kotlin.test.assertNull

class MCJarsApiProviderIntegrationTest {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val downloader = CachedDownloader.global(Downloader.http(), scope)

    private val provider = MCJarsApiProvider(downloader)

    @Test
    fun `test that MinecraftJarProvider#get works`() {
        val destination = WORK_DIR.resolve("${PLATFORM.name.lowercase()}-$VERSION.jar")
        destination.deleteIfExists()

        runBlocking { provider.get(PLATFORM, VERSION, destination.parent) }
        Assertions.assertTrue(destination.exists(), "JAR file does not exist: ${destination.toAbsolutePath()}")
    }

    @Test
    fun `test that MinecraftJarProvider#get throws if jar is not found`() {
        val version = "1.8.8-not-found"
        val destination = WORK_DIR.resolve("${PLATFORM.name.lowercase()}-$version.jar")

        assertThrows<JarNotFoundException> { provider.get(PLATFORM, version, destination.parent) }
    }

    @Test
    fun `test that MinecraftConfigProvider#get works`() {
        val destination = WORK_DIR.resolve("server.properties")
        destination.deleteIfExists()

        runBlocking { provider.get(destination.name, PLATFORM, VERSION, destination.parent) }
        Assertions.assertTrue(
            destination.exists(),
            "configuration file does not exist: ${destination.toAbsolutePath()}"
        )
    }

    @Test
    fun `test that MinecraftConfigProvider#get throws if jar is not found`() {
        val version = "1.8.8-not-found"
        val destination = WORK_DIR.resolve("server.properties")

        assertThrows<ConfigurationNotFoundException> {
            runBlocking { provider.get(destination.name, PLATFORM, version, destination.parent) }
        }
    }

    @Test
    fun `test getBuild internal cache`() {
        val provider = spyk(provider)

        var actual = provider.getBuild(PLATFORM, VERSION)
        Assertions.assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
        verify(exactly = 1) { provider.getFromApi(any()) }

        actual = provider.getBuild(PLATFORM, VERSION)
        Assertions.assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
        verify(exactly = 1) { provider.getFromApi(any()) }
    }

    @Test
    fun `test that getBuild returns correct data`() {
        val actual = provider.getBuild(PLATFORM, VERSION)
        Assertions.assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
    }

    @Test
    fun `test that getConfig returns correct data`() {
        val expectedConfig = EXPECTED_CONFIG
        val actual = provider.getConfig(expectedConfig.name, PLATFORM, VERSION)
        Assertions.assertEquals(expectedConfig, actual, "config data was not equal")
    }

    @Test
    fun `test that getFromApi of not found returns null`() {
        assertNull(provider.getFromApi("not-found"))
    }

    companion object {
        private val WORK_DIR = Path.of("build/resources/test/provider/mcjars_api_provider_test")

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