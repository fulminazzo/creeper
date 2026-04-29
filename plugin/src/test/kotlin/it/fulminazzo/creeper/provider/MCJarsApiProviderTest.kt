package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ServerType
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.uuid.Uuid

class MCJarsApiProviderTest {
    private val provider = MCJarsApiProvider()

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

}