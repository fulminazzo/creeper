package it.fulminazzo.creeper.server

import it.fulminazzo.creeper.PlayerProfile
import it.fulminazzo.creeper.PlayerResolver
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.provider.ConfigProvider
import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.server.spec.MinecraftServerSpec
import it.fulminazzo.creeper.server.spec.settings.MinecraftServerSettings
import org.slf4j.Logger
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * Special implementation of [ServerInstaller] for Minecraft servers.
 *
 * @property playerResolver the resolver to use for obtaining player ids
 * @constructor Creates a new Minecraft server installer
 *
 * @param specification the specification of the server to install
 * @param logger the logger to use for logging
 * @param jarProvider the provider of the server jar
 * @param configProvider the provider of the server configurations
 * @param downloader the downloader to use for downloading the plugins
 */
class MinecraftServerInstaller(
    specification: MinecraftServerSpec,
    logger: Logger,
    jarProvider: JarProvider<ServerType.MinecraftType>,
    configProvider: ConfigProvider<ServerType.MinecraftType>,
    downloader: CachedDownloader,
    private val playerResolver: PlayerResolver
) : ServerInstaller<ServerType.MinecraftType, MinecraftServerSettings, MinecraftServerSpec>(
    specification,
    logger,
    jarProvider,
    configProvider,
    downloader
) {

    override fun install(directory: Path): CompletableFuture<Path> {
        return super.install(directory)
            .thenCompose { executable ->
                installAndEditConfig(SERVER_PROPERTIES, directory) {
                    val settings = specification.settings
                    put("hardcore", settings.hardcore)
                    put("server-port", settings.port)
                    put("max-players", settings.players)
                    put("difficulty", settings.difficulty.name.lowercase())
                    put("gamemode", settings.gamemode.name.lowercase())
                    put("generate-structures", settings.generateStructures)
                    put("online-mode", settings.onlineMode)
                    put("spawn-protection", settings.spawnProtection)
                    put("view-distance", settings.viewDistance)
                    put("simulation-distance", settings.simulationDistance)
                    put("white-list", settings.whitelist)
                }.thenApply {
                    writeEula(executable.parent)
                    writeWhitelist(executable.parent)
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

    /**
     * Writes the whitelist file in the given directory.
     *
     * @param directory the directory where the whitelist file will be written
     */
    private fun writeWhitelist(directory: Path) {
        val whitelist = specification.whitelist
        if (whitelist.isNotEmpty()) {
            logger.info("Adding players to whitelist: ${whitelist.joinToString()}")
            val whitelistFile = directory.resolve("whitelist.json")
            val profiles = playerResolver.getPlayerProfiles(whitelist, specification.settings.onlineMode)
            profiles.takeIf { it.isNotEmpty() }?.let {
                JSON_MAPPER.writeValue(whitelistFile.toFile(), it)
            }
        }
    }

    private companion object {
        private const val SERVER_PROPERTIES = "server.properties"

        private val JSON_MAPPER = jacksonObjectMapper()

    }

}