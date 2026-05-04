import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.settings.Difficulty
import it.fulminazzo.creeper.extension.spec.settings.Gamemode
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.provider.plugin.GitHubPluginRequest
import it.fulminazzo.creeper.provider.plugin.HttpPluginRequest
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.provider.plugin.ModrinthPluginRequest
import it.fulminazzo.creeper.task.server.install.InstallServerTaskRegistrar
import java.nio.file.Path

plugins {
    id("it.fulminazzo.creeper")
}

val serverSpec = MinecraftServerSpec(
    ServerType.PAPER,
    "1.21",
    MinecraftServerSettings(
        25567,
        22,
        "",
        eula = true,
        whitelist = true,
        hardcore = true,
        difficulty = Difficulty.HARD,
        gamemode = Gamemode.CREATIVE,
        generateStructures = true,
        onlineMode = true,
        spawnProtection = 2,
        viewDistance = 3,
        simulationDistance = 4
    ),
    setOf("Notch", "jeb_"),
    setOf("jeb_"),
    listOf(
        ModrinthPluginRequest("teleporteffects", "3.0", "TeleportEffects-3.0.jar"),
        GitHubPluginRequest("fulminazzo", "YAGL", "5.2.2", "YAGL-plugin-5.2.2.jar"),
        HttpPluginRequest("https://github.com/fulminazzo/YAGL/releases/download/5.2.2/YAGL-5.2.2.jar"),
        LocalPluginRequest(Path.of("src/integrationTest/resources/task/server/install/Local-1.0.jar"), true)
    )
)

InstallServerTaskRegistrar.register(project, serverSpec, project.projectDir.toPath())