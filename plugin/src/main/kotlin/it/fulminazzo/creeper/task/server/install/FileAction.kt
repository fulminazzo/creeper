package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.CreeperPlugin.Companion.JSON_MAPPER
import it.fulminazzo.creeper.PlayerResolver
import it.fulminazzo.creeper.server.spec.MinecraftServerSpec
import it.fulminazzo.creeper.server.spec.ServerSpec
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * Represents an action to be performed on a raw server file.
 */
sealed class FileAction {

    /**
     * Applies this action to the given configuration.
     *
     * @param directory the directory where the file is located
     * @param specification the specification that requested the configuration
     * @param playerResolver the player resolver to fetch player profiles
     */
    abstract fun apply(directory: Path, specification: ServerSpec<*, *>, playerResolver: PlayerResolver)

    /**
     * Writes the `eula.txt` file.
     */
    data object Eula : FileAction() {

        override fun apply(directory: Path, specification: ServerSpec<*, *>, playerResolver: PlayerResolver) {
            val eulaFile = directory.createDirectories().resolve("eula.txt")
            eulaFile.toFile().writeText(
                """#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).
            |eula=true""".trimMargin()
            )
        }

    }

    /**
     * Writes the `whitelist.json` file.
     */
    data object Whitelist : FileAction() {

        override fun apply(directory: Path, specification: ServerSpec<*, *>, playerResolver: PlayerResolver) {
            if (specification is MinecraftServerSpec) {
                val whitelist = specification.whitelist
                if (whitelist.isNotEmpty()) {
                    val whitelistFile = directory.resolve("whitelist.json")
                    val profiles = playerResolver.getPlayerProfiles(whitelist, specification.settings.onlineMode)
                    profiles.takeIf { it.isNotEmpty() }?.let {
                        whitelistFile.parent.createDirectories()
                        JSON_MAPPER.writeValue(whitelistFile.toFile(), it)
                    }
                }
            }
        }

    }

    /**
     * Writes the `ops.json` file.
     */
    data object Operators : FileAction() {

        override fun apply(directory: Path, specification: ServerSpec<*, *>, playerResolver: PlayerResolver) {
            if (specification is MinecraftServerSpec) {
                val operators = specification.operators
                if (operators.isNotEmpty()) {
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
        }

    }

}