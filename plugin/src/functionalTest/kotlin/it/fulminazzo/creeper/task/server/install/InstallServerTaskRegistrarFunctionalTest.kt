package it.fulminazzo.creeper.task.server.install

import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.PlayerProfile
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstallServerTaskRegistrarFunctionalTest {
    private val projectDir = File("build/resources/functionalTest/task/server/install/server")

    private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }

    private val runner = GradleRunner.create()
        .forwardOutput()
        .withPluginClasspath()
        .withProjectDir(projectDir)

    @BeforeEach
    fun setup() {
        projectDir.deleteRecursively()
        projectDir.mkdirs()
        settingsFile.writeText("")
        buildFile.writeText(RESOURCE_BUILD_FILE.toFile().readText())
    }

    @Test
    fun `test that install task correctly installs server`() {
        runner.withArguments("installPaper1_21").build()

        val serverDir = projectDir.resolve("paper-1.21")
        assertTrue(serverDir.exists(), "Server directory does not exist: $serverDir")

        val jarFile = serverDir.resolve("paper-1.21.jar")
        assertTrue(jarFile.exists(), "Server jar does not exist: $jarFile")

        val eulaFile = serverDir.resolve("eula.txt")
        assertTrue(eulaFile.exists(), "EULA file does not exist: $eulaFile")
        assertContains(
            eulaFile.readLines(),
            "eula=true",
            "EULA has not been accepted"
        )

        val serverProperties = serverDir.resolve("server.properties")
        assertTrue(serverProperties.exists(), "Server properties file does not exist: $serverProperties")
        val properties = CreeperPlugin.PROPERTIES_MAPPER.readValue<Map<String, String>>(serverProperties)
        assertEquals("25567", properties["server-port"], "Server port was not set correctly")
        assertEquals("22", properties["players"], "Max players was not set correctly")
        assertEquals("true", properties["white-list"], "White list was not set correctly")
        assertEquals("true", properties["hardcore"], "Hardcore mode was not set correctly")
        assertEquals("hard", properties["difficulty"], "Difficulty was not set correctly")
        assertEquals("creative", properties["gamemode"], "Game mode was not set correctly")
        assertEquals("true", properties["generate-structures"], "Generate structures was not set correctly")
        assertEquals("true", properties["online-mode"], "Online mode was not set correctly")
        assertEquals("2", properties["spawn-protection"], "Spawn protection was not set correctly")
        assertEquals("3", properties["view-distance"], "View distance was not set correctly")
        assertEquals("4", properties["simulation-distance"], "Simulation distance was not set correctly")

        val whitelistFile = serverDir.resolve("whitelist.json")
        assertTrue(whitelistFile.exists(), "Whitelist file does not exist: $whitelistFile")
        val whitelist = CreeperPlugin.JSON_MAPPER.readValue<Set<PlayerProfile>>(whitelistFile)
        assertEquals(setOf(NOTCH, JEB), whitelist, "Whitelist does not contain expected players")

        val operatorsFile = serverDir.resolve("ops.json")
        assertTrue(operatorsFile.exists(), "Operators file does not exist: $operatorsFile")
        val operators = CreeperPlugin.JSON_MAPPER.readValue<Set<Map<String, String>>>(operatorsFile)
        assertEquals(
            setOf(
                mapOf(
                    "uuid" to JEB.id.toString(),
                    "name" to JEB.name,
                    "level" to "4",
                    "bypassesPlayerLimit" to "false"
                )
            ),
            operators,
            "Operators does not contain expected players"
        )

        val bukkitFile = serverDir.resolve("bukkit.yml")
        assertTrue(bukkitFile.exists(), "Bukkit config file does not exist: $bukkitFile")
        val bukkitConfig = CreeperPlugin.PROPERTIES_MAPPER.readValue<Map<String, Any>>(bukkitFile)
        @Suppress("UNCHECKED_CAST")
        assertEquals(
            "true",
            (bukkitConfig["settings"] as Map<String, Any>)["allow-end"],
            "Allow end was not set correctly"
        )

        val modrinthPlugin = serverDir.resolve("plugins").resolve("TeleportEffects-3.0.jar")
        assertTrue(modrinthPlugin.exists(), "Modrinth plugin file does not exist: $modrinthPlugin")

        val githubPlugin = serverDir.resolve("plugins").resolve("YAGL-plugin-5.2.2.jar")
        assertTrue(githubPlugin.exists(), "Github plugin file does not exist: $githubPlugin")

        val httpPlugin = serverDir.resolve("plugins").resolve("YAGL-5.2.2.jar")
        assertTrue(httpPlugin.exists(), "Http plugin file does not exist: $httpPlugin")

        val localPlugin = serverDir.resolve("plugins").resolve("Local-1.0.jar")
        assertTrue(localPlugin.exists(), "Local plugin file does not exist: $localPlugin")
    }

    private companion object {
        private val RESOURCE_BUILD_FILE = Path.of("src/functionalTest/resources/task/server/install/build.gradle.kts")

        private val NOTCH = PlayerProfile(
            UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"),
            "Notch"
        )

        private val JEB = PlayerProfile(
            UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"),
            "jeb_"
        )

    }

}