package it.fulminazzo.creeper.server.spec

import it.fulminazzo.creeper.server.ServerType
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class MinecraftServerSpecBuilderTest {

    @Test
    fun `test that build returns correct default values`() {
        val builder = MinecraftServerSpecBuilder()
        builder.type = ServerType.VANILLA
        builder.version = "1.16.5"
        builder.whitelist("Fulminazzo")
        builder.whitelist("xca_mux")
        builder.serverConfig {
            eula = true
            port = 25567
        }
        val data = builder.build()

        assertEquals(ServerType.VANILLA, data.type)
        assertEquals("1.16.5", data.version)
        assertEquals(setOf("Fulminazzo", "xca_mux"), data.whitelist)
        assertEquals(25567, data.config.port)
    }

    @Test
    fun `test that build with no type throws`() {
        val builder = MinecraftServerSpecBuilder()
        builder.version = "1.16.5"
        builder.serverConfig { eula = true }
        assertThrows<BuildException> { builder.build() }
    }

    @Test
    fun `test that build with no version throws`() {
        val builder = MinecraftServerSpecBuilder()
        builder.type = ServerType.VANILLA
        builder.serverConfig { eula = true }
        assertThrows<BuildException> { builder.build() }
    }

}