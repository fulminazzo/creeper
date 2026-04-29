package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ServerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class MCJarsApiProviderTest {
    private val provider = MCJarsApiProvider()

    @Test
    fun `test that get works`() {
        val destination = WORK_DIR.resolve("${PLATFORM.name.lowercase()}-$VERSION.jar")
        destination.deleteIfExists()

        provider.get(PLATFORM, VERSION, destination)
        assertTrue(destination.exists(), "destination file does not exist: ${destination.toAbsolutePath()}")
    }

    @Test
    fun `test that get throws if jar is not found`() {
        val version = "1.8.8-not-found"
        val destination = WORK_DIR.resolve("${PLATFORM.name.lowercase()}-$version.jar")

        assertThrows<JarNotFoundException> { provider.get(PLATFORM, version, destination) }
    }

        assertThrows<JarNotFoundException> { provider.get(platform, version, destination) }
    }

    @Test
    fun `test that getBuild returns correct data`() {
        val actual = provider.getBuild(PLATFORM, VERSION)
        assertEquals(EXPECTED_RESPONSE, actual, "build data was not equal")
    }

    @Test
    fun `test that getFromApi of not found returns null`() {
        assertNull(provider.getFromApi("not-found"))
    }

    companion object {
        private val WORK_DIR = Path.of("build/resources/test/mcjars_api_provider_test")

        private val PLATFORM = ServerType.VANILLA
        private const val VERSION = "1.8.8"
        private val EXPECTED_RESPONSE = BuildResponse(
            Uuid.parse("4e36bf73-fa69-4bd1-b86a-24de658ae2e6"),
            8322852L,
            "https://launcher.mojang.com/v1/objects/5fafba3f58c40dc51b5c3ca72a98f62dfdae1db7/server.jar"
        )

    }

}