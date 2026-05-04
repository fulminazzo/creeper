package it.fulminazzo.creeper.extension.spec.settings

import it.fulminazzo.creeper.extension.ExtensionTestHelper
import it.fulminazzo.creeper.util.mb
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class MinecraftServerSettingsBuilderTest : ExtensionTestHelper() {
    private val builder = objects.newInstance(MinecraftServerSettingsBuilder::class.java)

    @Test
    fun `test that build returns correct default values`() {
        builder.eula.set(true)
        builder.spawnProtection.set(5)
        builder.flags {
            it.minimumRam.set(1.mb)
        }
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
        assertContains(config.flags, "Xms1M")
    }

    @ParameterizedTest
    @MethodSource("provideBuildTests")
    fun `test that build fails with invalid values`(test: (MinecraftServerSettingsBuilder) -> Unit) {
        builder.eula.set(true)
        assertThrows<GradleException> {
            test(builder)
            builder.build()
        }
    }

    companion object {

        @JvmStatic
        fun provideBuildTests(): Stream<Arguments> {
            return Stream.of(
                // ServerSettingsBuilder
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port.set(Int.MIN_VALUE) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port.set(-1) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port.set(0) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port.set(65536) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.port.set(Int.MAX_VALUE) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.maximumPlayers.set(Int.MIN_VALUE) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.maximumPlayers.set(-1) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.maximumPlayers.set(0) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.minimumRam.set(0.mb) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.maximumRam.set(0.mb) }),
                // MinecraftServerSettingsBuilder
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.eula.set(false) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.spawnProtection.set(Int.MIN_VALUE) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.spawnProtection.set(-1) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.viewDistance.set(Int.MIN_VALUE) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.viewDistance.set(-1) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.viewDistance.set(0) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.simulationDistance.set(Int.MIN_VALUE) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.simulationDistance.set(-1) }),
                Arguments.of({ b: MinecraftServerSettingsBuilder -> b.simulationDistance.set(0) }),
            )
        }

    }

}