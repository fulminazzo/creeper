package it.fulminazzo.creeper.server

import it.fulminazzo.creeper.provider.ConfigProvider
import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.server.spec.MinecraftServerSpec
import it.fulminazzo.creeper.server.spec.settings.MinecraftServerSettings
import org.gradle.api.logging.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * Special implementation of [ServerInstaller] for Minecraft servers.
 *
 * @constructor Creates a new Minecraft server installer
 *
 * @param specification the specification of the server to install
 * @param logger the logger to use for logging
 * @param jarProvider the provider of the server jar
 * @param configProvider the provider of the server configurations
 */
class MinecraftServerInstaller(
    specification: MinecraftServerSpec,
    logger: Logger,
    jarProvider: JarProvider<ServerType.MinecraftType>,
    configProvider: ConfigProvider<ServerType.MinecraftType>
) : ServerInstaller<ServerType.MinecraftType, MinecraftServerSettings, MinecraftServerSpec>(
    specification,
    logger,
    jarProvider,
    configProvider
) {

    override fun install(directory: Path): CompletableFuture<Path> {
        return super.install(directory)
            .thenCompose { executable ->
                installAndEditConfig(SERVER_PROPERTIES, directory) {
                    val config = specification.config
                    put("server-port", config.port)
                    put("max-players", config.players)
                    put("difficulty", config.difficulty.name.lowercase())
                    put("gamemode", config.gamemode.name.lowercase())
                    put("generate-structures", config.generateStructures)
                    put("online-mode", config.onlineMode)
                    put("spawn-protection", config.spawnProtection)
                    put("view-distance", config.viewDistance)
                    put("simulation-distance", config.simulationDistance)
                }.thenApply {
                    writeEula(directory)
                    executable
                }
            }
    }

    /**
     * Writes the EULA file in the given directory.
     *
     * @param directory the directory where the EULA file will be written
     */
    private fun writeEula(directory: Path) {
        val eulaFile = directory.resolve("eula.txt")
        eulaFile.toFile().writeText(
            """#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).
            |eula=true""".trimMargin()
        )
    }

    private companion object {
        private const val SERVER_PROPERTIES = "server.properties"

    }

}