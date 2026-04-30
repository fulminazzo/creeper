package it.fulminazzo.creeper.server.config

import it.fulminazzo.creeper.server.BuildException
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class MinecraftServerConfigBuilderTest {

    @ParameterizedTest
    @MethodSource("provideBuildTests")
    fun `test that build fails with invalid values`(test: (MinecraftServerConfigBuilder) -> Unit) {
        val builder = MinecraftServerConfigBuilder()
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