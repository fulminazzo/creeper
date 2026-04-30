package it.fulminazzo.creeper.server.config

import it.fulminazzo.creeper.server.BuildException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test

class MinecraftServerConfigBuilderTest {
    private val builder = MinecraftServerConfigBuilder()

    @Test
    fun `test that build returns correct default values`() {
        builder.eula = true
        val config = builder.build()

        assertEquals(25565, config.port)
        assertEquals(20, config.players)
        assertFalse(config.whitelist)
        assertEquals(Difficulty.PEACEFUL, config.difficulty)
        assertEquals(Gamemode.SURVIVAL, config.gamemode)
        assertFalse(config.generateStructures)
        assertFalse(config.onlineMode)
        assertEquals(0, config.spawnProtection)
        assertEquals(2, config.viewDistance)
        assertEquals(2, config.simulationDistance)
    }

    @ParameterizedTest
    @MethodSource("provideBuildTests")
    fun `test that build fails with invalid values`(test: (MinecraftServerConfigBuilder) -> Unit) {
        builder.eula = true
        assertThrows<BuildException> {
            test(builder)
            builder.build()
        }
    }

    companion object {

        @JvmStatic
        fun provideBuildTests(): Stream<Arguments> {
            return Stream.of(
                // ServerConfigBuilder
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.port = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.port = -1 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.port = 0 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.port = 65536 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.port = Int.MAX_VALUE }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.players = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.players = -1 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.players = 0 }),
                // MinecraftServerConfigBuilder
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.eula = false }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.spawnProtection = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.spawnProtection = -1 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.viewDistance = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.viewDistance = -1 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.viewDistance = 0 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.simulationDistance = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.simulationDistance = -1 }),
                Arguments.of({ b: MinecraftServerConfigBuilder -> b.simulationDistance = 0 }),
            )
        }

    }

}