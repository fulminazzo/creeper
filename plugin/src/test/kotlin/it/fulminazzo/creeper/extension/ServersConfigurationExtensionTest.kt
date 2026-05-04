package it.fulminazzo.creeper.extension

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.settings.Difficulty
import it.fulminazzo.creeper.extension.spec.settings.Gamemode
import it.fulminazzo.creeper.extension.spec.settings.JvmFlagsBuilder
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import it.fulminazzo.creeper.provider.plugin.HttpPluginRequest
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.provider.plugin.ModrinthPluginRequest
import it.fulminazzo.creeper.util.mb
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import java.net.URI
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertIs

class ServersConfigurationExtensionTest {

    @Test
    fun `test that minecraftServer correctly adds configured server specification`() {
        val expected = MinecraftServerSpec(
            ServerType.PAPER,
            "26.1",
            MinecraftServerSettings(
                port = 25566,
                players = 11,
                flags = "-Xms256M -Xmx10G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:+UnlockExperimentalVMOptions " +
                        "-XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+PerfDisableSharedMem -XX:+creeper " +
                        "-XX:MaxGCPauseMillis=200 -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 " +
                        "-XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 " +
                        "-XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 " +
                        "-XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 " +
                        "-XX:SurvivorRatio=32 -XX:MaxTenuringThreshold=1 -XX:tester-data=1.0 " +
                        "-Dcreeper.version=0.0.1-SNAPSHOT",
                eula = true,
                whitelist = true,
                hardcore = true,
                difficulty = Difficulty.HARD,
                gamemode = Gamemode.ADVENTURE,
                generateStructures = true,
                onlineMode = true,
                spawnProtection = 128,
                viewDistance = 8,
                simulationDistance = 6
            ),
            setOf("Fulminazzo", "xca_mux"),
            setOf("xca_mux"),
            listOf(
                ModrinthPluginRequest("teleporteffects", "3.0", "TeleportEffects-3.0.jar"),
                GitHubPluginRequest("fulminazzo", "YAGL", "5.2.2", "YAGL-5.2.2.jar"),
                HttpPluginRequest("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"),
                LocalPluginRequest(Path.of("build/libs/YAGL-5.2.2.jar"), false)
            )
        )

        val project = ProjectBuilder.builder().build()
        val extension = project.extensions.create(
            "minecraftServers",
            ServersConfigurationExtension::class.java
        )

        extension.minecraftServer { specBuilder ->
            specBuilder.type.set("paper")
            specBuilder.version.set("26.1")
            specBuilder.serverConfig { settingsBuilder ->
                settingsBuilder.port.set(25566)
                settingsBuilder.maximumPlayers.set(11)
                settingsBuilder.flags {
                    it.minRam(256, "m")
                    it.maxRam(10, "gb")
                    it.xx("creeper", true)
                    it.xx("tester-data", 1.0)
                    it.property("creeper.version", ProjectInfo.VERSION)
                }
                settingsBuilder.eula.set(true)
                settingsBuilder.whitelist.set(true)
                settingsBuilder.hardcore.set(true)
                settingsBuilder.difficulty.set("hard")
                settingsBuilder.gamemode.set("adventure")
                settingsBuilder.generateStructures.set(true)
                settingsBuilder.onlineMode.set(true)
                settingsBuilder.spawnProtection.set(128)
                settingsBuilder.viewDistance.set(8)
                settingsBuilder.simulationDistance.set(6)
            }
            specBuilder.whitelist("Fulminazzo", "xca_mux")
            specBuilder.ops("xca_mux")
            specBuilder.plugins {
                it.modrinth("teleporteffects", "3.0", "TeleportEffects-3.0.jar")
                it.github("fulminazzo", "YAGL", "5.2.2", "YAGL-5.2.2.jar")
                it.url("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar")
                it.local("build/libs/YAGL-5.2.2.jar", false)
            }
        }

        val actual = extension.specifications.first()
        assertIs<MinecraftServerSpec>(actual)
        assertEquals(expected.type, actual.type, "Invalid server type")
        assertEquals(expected.version, actual.version, "Invalid server version")
        assertEquals(expected.settings.port, actual.settings.port, "Invalid server settings port")
        assertEquals(expected.settings.players, actual.settings.players, "Invalid server settings players")
        assertEquals(expected.settings.flags, actual.settings.flags, "Invalid server settings flags")
        assertEquals(expected.settings.eula, actual.settings.eula, "Invalid server settings EULA")
        assertEquals(expected.settings.hardcore, actual.settings.hardcore, "Invalid server settings hardcore")
        assertEquals(expected.settings.difficulty, actual.settings.difficulty, "Invalid server settings difficulty")
        assertEquals(expected.settings.gamemode, actual.settings.gamemode, "Invalid server settings gamemode")
        assertEquals(expected.settings.onlineMode, actual.settings.onlineMode, "Invalid server settings onlineMode")
        assertEquals(
            expected.settings.generateStructures,
            actual.settings.generateStructures,
            "Invalid server settings generateStructures"
        )
        assertEquals(
            expected.settings.spawnProtection,
            actual.settings.spawnProtection,
            "Invalid server settings spawnProtection"
        )
        assertEquals(
            expected.settings.viewDistance,
            actual.settings.viewDistance,
            "Invalid server settings viewDistance"
        )
        assertEquals(
            expected.settings.simulationDistance,
            actual.settings.simulationDistance,
            "Invalid server settings simulationDistance"
        )
        assertEquals(expected.whitelist, actual.whitelist, "Invalid whitelist")
        assertEquals(expected.operators, actual.operators, "Invalid operators")
        assertEquals(expected.plugins, actual.plugins, "Invalid plugins")
    }

}