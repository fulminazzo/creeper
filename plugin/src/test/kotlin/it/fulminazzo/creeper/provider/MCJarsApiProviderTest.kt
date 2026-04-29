package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ServerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class MCJarsApiProviderTest {
    private val workDir = Path.of("build/resources/test/mcjars_api_provider_test")

    private val provider = MCJarsApiProvider()

    @Test
    fun `test that get build works`() {
        val platform = ServerType.VANILLA
        val version = "1.8.8"
        val destination = workDir.resolve("${platform.name.lowercase()}-$version.jar")
        destination.deleteIfExists()

        provider.get(platform, version, destination)
        assertTrue(destination.exists(), "destination file does not exist: ${destination.toAbsolutePath()}")
    }

    @Test
    fun `test that get build throws if jar is not found`() {
        val platform = ServerType.VANILLA
        val version = "1.8.8-not-found"
        val destination = workDir.resolve("${platform.name.lowercase()}-$version.jar")

        assertThrows<JarNotFoundException> { provider.get(platform, version, destination) }
    }

    @Test
    fun `test that getBuild returns correct data`() {
        val expected = BuildResponse(
            Uuid.parse("4e36bf73-fa69-4bd1-b86a-24de658ae2e6"),
            8322852L,
            "https://launcher.mojang.com/v1/objects/5fafba3f58c40dc51b5c3ca72a98f62dfdae1db7/server.jar"
        )

        val actual = provider.getBuild(ServerType.VANILLA, "1.8.8")
        assertEquals(expected, actual, "build data was not equal")
    }

    @Test
    fun `test that getFromApi of not found returns null`() {
        assertNull(provider.getFromApi("not-found"))
    }

}