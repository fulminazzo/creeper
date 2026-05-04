package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.settings.Difficulty
import it.fulminazzo.creeper.extension.spec.settings.Gamemode
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class ConfigActionTest {
    private val configuration = mutableMapOf<String, Any>()

    @Test
    fun `test that ServerProperties ConfigAction configures correct values`() {
        val specification = MinecraftServerSpec(
            config = MinecraftServerSettings(
                hardcore = true,
                players = 2,
                onlineMode = true,
                spawnProtection = 5,
                generateStructures = true,
                whitelist = true
            )
        )
        ConfigAction.ServerProperties.apply(configuration, specification)
        assertEquals(true, configuration["hardcore"], "Hardcore should be true")
        assertEquals(25565, configuration["server-port"], "Server port should be 25565")
        assertEquals(2, configuration["max-players"], "Max players should be 2")
        assertEquals(true, configuration["online-mode"], "Online mode should be true")
        assertEquals(5, configuration["spawn-protection"], "Spawn protection should be 5")
        assertEquals(Difficulty.NORMAL.name.lowercase(), configuration["difficulty"], "Difficulty should be NORMAL")
        assertEquals(Gamemode.SURVIVAL.name.lowercase(), configuration["gamemode"], "Gamemode should be SURVIVAL")
        assertEquals(true, configuration["generate-structures"], "Generate structures should be true")
        assertEquals(2, configuration["view-distance"], "View distance should be 2")
        assertEquals(2, configuration["simulation-distance"], "Simulation distance should be 2")
        assertEquals(true, configuration["white-list"], "White list should be true")

        assertEquals(false, configuration["allow-nether"], "Allow nether should be false")
        assertEquals(ProjectInfo.MOTD, configuration["motd"], "MOTD should be ${ProjectInfo.MOTD}")
    }

    @Test
    fun `test that BukkitConfig ConfigAction configures correct values`() {
        ConfigAction.BukkitConfig.apply(configuration, MinecraftServerSpec())
        assertEquals(false, (configuration["settings"] as Map<*, *>)["allow-end"], "Allow end should be false")
    }

}