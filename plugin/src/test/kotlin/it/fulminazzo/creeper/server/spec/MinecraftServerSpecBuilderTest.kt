package it.fulminazzo.creeper.server.spec

import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import it.fulminazzo.creeper.provider.plugin.HttpPluginRequest
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.server.ServerType
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.nio.file.Path
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
        builder.plugins {
            github("fulminazzo", "YAGL", "5.2.2", "YAGL-5.2.2.jar")
            url(URI.create("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"))
            local("build/libs/YAGL-5.2.2.jar", false)
        }
        val data = builder.build() as MinecraftServerSpec

        assertEquals(ServerType.VANILLA, data.type)
        assertEquals("1.16.5", data.version)
        assertEquals(setOf("Fulminazzo", "xca_mux"), data.whitelist)
        assertEquals(25567, data.settings.port)
        assertEquals(
            listOf(
                GitHubPluginRequest("fulminazzo", "YAGL", "5.2.2", "YAGL-5.2.2.jar"),
                HttpPluginRequest("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"),
                LocalPluginRequest(Path.of("build/libs/YAGL-5.2.2.jar"), false)
            ),
            data.plugins
        )
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