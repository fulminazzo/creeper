package it.fulminazzo.creeper.server

import it.fulminazzo.creeper.ServerType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ServerTypeTest {

    @ParameterizedTest
    @MethodSource("provideTestServerTypes")
    fun `test that server type is fork of expected types`(serverType: ServerType, validServerTypes: List<ServerType>) {
        validServerTypes.forEach {
            assertTrue(serverType.isForkOf(it))
        }
    }

    private companion object {

        @JvmStatic
        fun provideTestServerTypes(): Stream<Arguments> = Stream.of(
            Arguments.of(
                ServerType.VANILLA,
                listOf(ServerType.VANILLA)
            ),
            Arguments.of(
                ServerType.BUKKIT,
                listOf(ServerType.VANILLA, ServerType.BUKKIT)
            ),
            Arguments.of(
                ServerType.SPIGOT,
                listOf(ServerType.VANILLA, ServerType.BUKKIT, ServerType.SPIGOT)
            ),
            Arguments.of(
                ServerType.PAPER,
                listOf(ServerType.VANILLA, ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER)
            ),
            Arguments.of(
                ServerType.PURPUR,
                listOf(ServerType.VANILLA, ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER, ServerType.PURPUR)
            ),
            Arguments.of(
                ServerType.FOLIA,
                listOf(ServerType.VANILLA, ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER, ServerType.FOLIA)
            ),
        )

    }

}