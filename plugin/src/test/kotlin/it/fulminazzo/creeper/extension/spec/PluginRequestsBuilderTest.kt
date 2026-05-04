package it.fulminazzo.creeper.extension.spec

import it.fulminazzo.creeper.extension.ExtensionTestHelper
import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import it.fulminazzo.creeper.provider.plugin.HttpPluginRequest
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.provider.plugin.ModrinthPluginRequest
import org.junit.jupiter.api.Assertions.*
import java.nio.file.Path
import kotlin.test.Test

class PluginRequestsBuilderTest : ExtensionTestHelper() {
    private val builder = objects.newInstance(PluginRequestsBuilder::class.java)

    @Test
    fun `test that modrinth correctly adds ModrinthPluginRequest`() {
        val expected = ModrinthPluginRequest("TeleportEffects", "3.0", "TeleportEffects-3.0.jar")
        builder.modrinth(expected.projectName, expected.version, expected.name)
        assertEquals(expected, builder.requests.first())
    }

    @Test
    fun `test that github correctly adds GitHubPluginRequest`() {
        val expected = GitHubPluginRequest("fulminazzo", "YAGL", "5.2.2", "YAGL-5.2.2.jar")
        builder.github(expected.owner, expected.repository, expected.release, expected.filename)
        assertEquals(expected, builder.requests.first())
    }

    @Test
    fun `test that url correctly adds HttpPluginRequest`() {
        val expected = HttpPluginRequest("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar")
        builder.url(expected.url)
        assertEquals(expected, builder.requests.first())
    }

    @Test
    fun `test that local correctly adds LocalPluginRequest`() {
        val expected = LocalPluginRequest(Path.of("build/libs/YAGL-5.2.2.jar"), false)
        builder.local(expected.file, expected.overwrite)
    }

}