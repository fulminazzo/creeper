package it.fulminazzo.creeper.server.spec.settings

import it.fulminazzo.creeper.server.spec.BuildException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test

class MinecraftServerSettingsBuilderTest {
    private val builder = MinecraftServerSettingsBuilder()

    @Test
    fun `test that build returns correct default values`() {
        builder.eula = true
        builder.spawnProtection = 5
        val config = builder.build()

        assertEquals(25565, config.port)
        assertEquals(20, config.players)
        assertFalse(config.whitelist)
        assertEquals(Difficulty.PEACEFUL, config.difficulty)
        assertEquals(Gamemode.SURVIVAL, config.gamemode)
        assertFalse(config.generateStructures)
        assertFalse(config.onlineMode)
        assertEquals(5, config.spawnProtection)
        assertEquals(2, config.viewDistance)
        assertEquals(2, config.simulationDistance)
    }

    @ParameterizedTest
    @MethodSource("provideBuildTests")
    fun `test that build fails with invalid values`(test: (MinecraftServerSettingsBuilder) -> Unit) {
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
                // ServerSettingsBuilder
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port = -1 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port = 0 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port = 65536 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port = Int.MAX_VALUE }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.players = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.players = -1 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.players = 0 }),
                // MinecraftServerSettingsBuilder
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.eula = false }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.spawnProtection = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.spawnProtection = -1 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.viewDistance = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.viewDistance = -1 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.viewDistance = 0 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.simulationDistance = Int.MIN_VALUE }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.simulationDistance = -1 }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.simulationDistance = 0 }),
            )
        }

    }

}