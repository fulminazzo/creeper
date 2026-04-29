package it.fulminazzo.creeper.provider

import io.mockk.spyk
import io.mockk.verify
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

        provider.get(PLATFORM, VERSION, destination.parent)
        assertTrue(destination.exists(), "destination file does not exist: ${destination.toAbsolutePath()}")
    }

    @Test
    fun `test that get throws if jar is not found`() {
        val version = "1.8.8-not-found"
        val destination = WORK_DIR.resolve("${PLATFORM.name.lowercase()}-$version.jar")

        assertThrows<JarNotFoundException> { provider.get(PLATFORM, version, destination.parent) }
    }

    @Test
    fun `test getBuild internal cache`() {
        val provider = spyk<MCJarsApiProvider>()

        var actual = provider.getBuild(PLATFORM, VERSION)
        assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
        verify(exactly = 1) { provider.getFromApi(any()) }

        actual = provider.getBuild(PLATFORM, VERSION)
        assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
        verify(exactly = 1) { provider.getFromApi(any()) }
    }

    @Test
    fun `test that getBuild returns correct data`() {
        val actual = provider.getBuild(PLATFORM, VERSION)
        assertEquals(EXPECTED_BUILD_RESPONSE, actual, "build data was not equal")
    }

    @Test
    fun `test that getConfig returns correct data`() {
        val expectedConfig = EXPECTED_CONFIG
        val actual = provider.getConfig(expectedConfig.name, PLATFORM, VERSION)
        assertEquals(expectedConfig, actual, "config data was not equal")
    }

    @Test
    fun `test that getFromApi of not found returns null`() {
        assertNull(provider.getFromApi("not-found"))
    }

    companion object {
        private val WORK_DIR = Path.of("build/resources/test/mcjars_api_provider_test")

        private val PLATFORM = ServerType.VANILLA
        private const val VERSION = "1.8.8"

        private val EXPECTED_BUILD_RESPONSE = BuildResponse(
            Uuid.parse("4e36bf73-fa69-4bd1-b86a-24de658ae2e6"),
            8322852L,
            "https://launcher.mojang.com/v1/objects/5fafba3f58c40dc51b5c3ca72a98f62dfdae1db7/server.jar"
        )
        private val EXPECTED_CONFIG = Config(
            Uuid.parse("ce8ba7dd-71fd-49ba-b31e-9466033e0ef4"),
            "server.properties",
            """allow-flight=false
allow-nether=true
announce-player-achievements=true
difficulty=1
enable-command-block=false
enable-query=false
enable-rcon=false
force-gamemode=false
gamemode=0
generate-structures=true
generator-settings=
level-name=world
level-seed=
level-type=DEFAULT
max-build-height=256
max-players=20
max-tick-time=60000
max-world-size=29999984
motd=A Minecraft Server
network-compression-threshold=256
online-mode=true
op-permission-level=4
player-idle-timeout=0
pvp=true
resource-pack-hash=
resource-pack=
server-ip=
server-port=25565
snooper-enabled=true
spawn-animals=true
spawn-monsters=true
spawn-npcs=true
use-native-transport=true
view-distance=10
white-list=false"""
        )

    }

}