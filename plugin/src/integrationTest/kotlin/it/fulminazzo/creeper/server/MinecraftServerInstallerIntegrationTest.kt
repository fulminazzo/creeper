package it.fulminazzo.creeper.server

import io.mockk.every
import io.mockk.mockk
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import it.fulminazzo.creeper.provider.ConfigProvider
import it.fulminazzo.creeper.provider.MinecraftJarProvider
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.server.spec.MinecraftServerSpec
import it.fulminazzo.creeper.server.spec.settings.Difficulty
import it.fulminazzo.creeper.server.spec.settings.Gamemode
import it.fulminazzo.creeper.server.spec.settings.MinecraftServerSettingsBuilder
import org.slf4j.LoggerFactory
import tools.jackson.dataformat.javaprop.JavaPropsMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MinecraftServerInstallerIntegrationTest {
    private val logger = LoggerFactory.getLogger(MinecraftServerInstallerIntegrationTest::class.java)

    @Test
    fun `test that install correctly downloads executable and sets configuration`() {
        DIRECTORY.toFile().deleteRecursively()
        val jarProvider = mockk<MinecraftJarProvider>()
        val expectedExecutable = DIRECTORY.resolve("server.jar")
        every { jarProvider.get(any(), any(), any()) }.answers {
            val path = DIRECTORY.resolve(arg<String>(0))
            path.deleteIfExists()
            path.parent.createDirectories()
            path.createFile()
            CompletableFuture.completedFuture(path)
        }

        val configProvider = mockk<ConfigProvider<ServerType.MinecraftType>>()
        every { configProvider.get(any(), any(), any(), any()) }.answers {
            val path = DIRECTORY.resolve(arg<String>(0))
            path.deleteIfExists()
            path.parent.createDirectories()
            path.createFile()
            CompletableFuture.completedFuture(path)
        }

        val settings = MinecraftServerSettingsBuilder()
        settings.eula = true
        settings.port = 25577
        settings.players = 21
        settings.difficulty = Difficulty.HARD
        settings.gamemode = Gamemode.CREATIVE
        settings.generateStructures = true
        settings.onlineMode = false
        settings.spawnProtection = 10
        settings.viewDistance = 6
        settings.simulationDistance = 7
        settings.whitelist = true
        val specification = MinecraftServerSpec(
            ServerType.VANILLA,
            "1.21.1",
            settings.build(),
            emptySet(),
            listOf(
                LocalPluginRequest(
                    Path.of("build/resources/integrationTest/server/Test-1.0.jar"),
                    true
                )
            )
        )

        val installer = MinecraftServerInstaller(
            specification,
            logger,
            jarProvider,
            configProvider,
            CachedDownloader.simple(Downloader.http())
        )

        val executable = installer.install(DIRECTORY).join()

        assertTrue(expectedExecutable.exists(), "$expectedExecutable does not exist")
        assertEquals(expectedExecutable, executable, "executable path was not set correctly")

        assertTrue(DIRECTORY.resolve("plugins/Test-1.0.jar").exists(), "plugin file does not exist")

        val eulaFile = DIRECTORY.resolve("eula.txt")
        assertTrue(eulaFile.exists(), "eula file does not exist")
        assertContains(eulaFile.toFile().readText(), "eula=true")

        val serverProperties = DIRECTORY.resolve("server.properties")
        assertTrue(serverProperties.exists(), "server.properties file does not exist")

        val data = PROPERTIES_MAPPER.readValue<Map<String, Any>>(serverProperties.toFile())
        assertEquals(settings.port.toString(), data["server-port"], "server-port was not set correctly")
        assertEquals(settings.players.toString(), data["max-players"], "max-players was not set correctly")
        assertEquals(settings.difficulty.name.lowercase(), data["difficulty"], "difficulty was not set correctly")
        assertEquals(settings.gamemode.name.lowercase(), data["gamemode"], "gamemode was not set correctly")
        assertEquals(
            settings.generateStructures.toString(),
            data["generate-structures"],
            "generate-structures was not set correctly"
        )
        assertEquals(settings.onlineMode.toString(), data["online-mode"], "online-mode was not set correctly")
        assertEquals(
            settings.spawnProtection.toString(),
            data["spawn-protection"],
            "spawn-protection was not set correctly"
        )
        assertEquals(settings.viewDistance.toString(), data["view-distance"], "view-distance was not set correctly")
        assertEquals(
            settings.simulationDistance.toString(),
            data["simulation-distance"],
            "simulation-distance was not set correctly"
        )
        assertEquals(settings.whitelist.toString(), data["white-list"], "white-list was not set correctly")
    }

    private companion object {
        private val DIRECTORY = Path.of("build/resources/integrationTest/server/minecraft_server_installer_test")

        private val PROPERTIES_MAPPER = JavaPropsMapper.builder().addModule(kotlinModule()).build()

    }

}