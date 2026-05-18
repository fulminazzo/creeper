import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.task.server.run.RunServerTaskRegistrar

plugins {
    id("it.fulminazzo.creeper")
}

val serverSpec = MinecraftServerSpec(
    ServerType.PAPER,
    "1.21",
    MinecraftServerSettings(
        25568,
        22,
        "-Xms1G -Xmx3G"
    ),
    setOf<String>(),
    setOf<String>(),
    listOf<PluginRequest>()
)

RunServerTaskRegistrar.register(
    project,
    serverSpec,
    project.projectDir.toPath().resolve("server-runner.jar"),
    project.projectDir.toPath()
)