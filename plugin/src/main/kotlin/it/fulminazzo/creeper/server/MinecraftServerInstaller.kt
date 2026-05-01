package it.fulminazzo.creeper.server

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
import java.util.concurrent.Executor
import kotlin.collections.mapOf
import kotlin.io.path.createDirectories

/**
 * Special implementation of [ServerInstaller] for Minecraft servers.
 *
 * @constructor Creates a new Minecraft server installer
 *
 * @param specification the specification of the server to install
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 * @param downloader the downloader to use for downloading the plugins
 * @param jarProvider the provider of the server jar
 * @param configProvider the provider of the server configurations
 */
class MinecraftServerInstaller(
    specification: MinecraftServerSpec,
    logger: Logger,
    executor: Executor,
    downloader: CachedDownloader,
    jarProvider: JarProvider<ServerType.MinecraftType>,
    configProvider: ConfigProvider<ServerType.MinecraftType>
) : ServerInstaller<ServerType.MinecraftType, MinecraftServerSettings, MinecraftServerSpec>(
    specification,
    logger,
    executor,
    downloader,
    jarProvider,
    configProvider
) {
    private val playerResolver = PlayerResolver(logger, executor)

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
                    writeOperators(executable.parent)
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
    internal fun writeWhitelist(directory: Path) {
        val whitelist = specification.whitelist
        if (whitelist.isNotEmpty()) {
            logger.info("Adding players to whitelist: ${whitelist.joinToString()}")
            val whitelistFile = directory.resolve("whitelist.json")
            val profiles = playerResolver.getPlayerProfiles(whitelist, specification.settings.onlineMode)
            profiles.takeIf { it.isNotEmpty() }?.let {
                whitelistFile.parent.createDirectories()
                JSON_MAPPER.writeValue(whitelistFile.toFile(), it)
            }
        }
    }

    /**
     * Writes the operators' file in the given directory.
     *
     * @param directory the directory where the operators' file will be written
     */
    internal fun writeOperators(directory: Path) {
        val operators = specification.operators
        if (operators.isNotEmpty()) {
            logger.info("Adding players to operators: ${operators.joinToString()}")
            val operatorsFile = directory.resolve("ops.json")
            val profiles = playerResolver.getPlayerProfiles(operators, specification.settings.onlineMode)
            profiles.takeIf { it.isNotEmpty() }?.let { operators ->
                operatorsFile.parent.createDirectories()
                JSON_MAPPER.writeValue(operatorsFile.toFile(), operators.map { operator ->
                    mapOf(
                        "uuid" to operator.id,
                        "name" to operator.name,
                        "level" to 4,
                        "bypassesPlayerLimit" to false
                    )
                })
            }
        }
    }

    private companion object {
        private const val SERVER_PROPERTIES = "server.properties"

        private val JSON_MAPPER = jacksonObjectMapper()

    }

}