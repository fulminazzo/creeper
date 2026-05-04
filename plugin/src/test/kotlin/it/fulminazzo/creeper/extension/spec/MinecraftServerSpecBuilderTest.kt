package it.fulminazzo.creeper.extension.spec

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import it.fulminazzo.creeper.extension.ExtensionTestHelper
import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import it.fulminazzo.creeper.provider.plugin.HttpPluginRequest
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.provider.plugin.ModrinthPluginRequest
import it.fulminazzo.creeper.ServerType
import org.gradle.api.GradleException
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class MinecraftServerSpecBuilderTest : ExtensionTestHelper() {
    private val builder = objects.newInstance(MinecraftServerSpecBuilder::class.java)

    @Test
    fun `test that build returns correct default values`() {
        builder.type.set(ServerType.VANILLA.name)
        builder.version.set("1.16.5")
        builder.whitelist("Fulminazzo")
        builder.whitelist("xca_mux")
        builder.ops("xca_mux")
        builder.serverConfig {
            it.eula.set(true)
            it.port.set(25567)
        }
        builder.plugins {
            it.modrinth("TeleportEffects", "3.0", "TeleportEffects-3.0.jar")
            it.github("fulminazzo", "YAGL", "5.2.2", "YAGL-5.2.2.jar")
            it.url(URI.create("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"))
            it.local("build/libs/YAGL-5.2.2.jar", false)
        }
        val data = builder.build()

        assertEquals(ServerType.VANILLA, data.type)
        assertEquals("1.16.5", data.version)
        assertEquals(setOf("Fulminazzo", "xca_mux"), data.whitelist)
        assertEquals(setOf("xca_mux"), data.operators)
        assertEquals(25567, data.settings.port)
        assertEquals(
            listOf(
                ModrinthPluginRequest("TeleportEffects", "3.0", "TeleportEffects-3.0.jar"),
                GitHubPluginRequest("fulminazzo", "YAGL", "5.2.2", "YAGL-5.2.2.jar"),
                HttpPluginRequest("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"),
                LocalPluginRequest(Path.of("build/libs/YAGL-5.2.2.jar"), false)
            ),
            data.plugins
        )
    }

    @Test
    fun `test that build with no type throws`() {
        builder.version.set("1.16.5")
        builder.serverConfig { it.eula.set(true) }
        assertThrows<GradleException> { builder.build() }
    }

    @Test
    fun `test that build with no version throws`() {
        builder.type.set(ServerType.VANILLA.name)
        builder.serverConfig { it.eula.set(true) }
        assertThrows<GradleException> { builder.build() }
    }

    @Test
    fun `test that build throws with non-applicable type`() {
        mockkObject(ServerType)
        every { ServerType.valueOf("unknown") } returns ServerType.UNKNOWN
        builder.type.set("unknown")
        builder.version.set("1.16.5")
        builder.serverConfig { it.eula.set(true) }
        assertThrows<GradleException> { builder.build() }
        unmockkObject(ServerType)
    }

    @Test
    fun `test that build throws with invalid type`() {
        builder.type.set("invalid")
        builder.version.set("1.16.5")
        builder.serverConfig { it.eula.set(true) }
        assertThrows<GradleException> { builder.build() }
    }

}