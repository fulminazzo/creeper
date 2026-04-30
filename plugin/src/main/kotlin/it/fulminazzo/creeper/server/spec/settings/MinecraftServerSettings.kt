package it.fulminazzo.creeper.server.spec.settings

import it.fulminazzo.creeper.server.spec.BuildException
import it.fulminazzo.creeper.server.spec.requireNatural
import it.fulminazzo.creeper.server.spec.requirePositive

/**
 * Holds all the settings for a Minecraft server to run.
 *
 * @property difficulty the difficulty of the server
 * @property gamemode the default gamemode of the server
 * @property generateStructures if `true` the server will generate structures
 * @property onlineMode if `true` only players with a premium account will be able to join
 * @property spawnProtection the destroy/build protection radius of the server
 * @property viewDistance the view distance of the server
 * @property simulationDistance the simulation distance of the server
 * @constructor Creates a new Minecraft server config
 *
 * @param eula if `false` the server will not start
 * @param port the port the server should run on
 * @param players the maximum number of players allowed
 * @param whitelist if `true` only players on the whitelist can join
 * @param flags the JVM flags to use
 */
class MinecraftServerSettings(
    eula: Boolean,
    port: Int,
    players: Int,
    whitelist: Boolean,
    flags: String,
    val difficulty: Difficulty,
    val gamemode: Gamemode,
    val generateStructures: Boolean,
    val onlineMode: Boolean,
    val spawnProtection: Int,
    val viewDistance: Int,
    val simulationDistance: Int
) : ServerSettings(
    eula,
    port,
    players,
    whitelist,
    flags
)

/**
 * Builder for [MinecraftServerSettings].
 *
 * @constructor Create a new Minecraft server settings builder
 */
class MinecraftServerSettingsBuilder : ServerSettingsBuilder() {
    var difficulty: Difficulty = Difficulty.PEACEFUL
    var gamemode: Gamemode = Gamemode.SURVIVAL
    var generateStructures: Boolean = false
    var onlineMode: Boolean = false
    var spawnProtection: Int = 0
        set(value) {
            field = value.requireNatural("spawn protection")
        }
    var viewDistance: Int = 2
        set(value) {
            field = value.requirePositive("view distance")
        }
    var simulationDistance: Int = 2
        set(value) {
            field = value.requirePositive("simulation distance")
        }

    init {
        flags.from(JvmFlagsBuilder.AKAIR_FLAGS)
    }

    override fun build(): MinecraftServerSettings {
        return if (eula) MinecraftServerSettings(
            true,
            port,
            players,
            whitelist,
            flags.build(),
            difficulty,
            gamemode,
            generateStructures,
            onlineMode,
            spawnProtection,
            viewDistance,
            simulationDistance
        ) else throw BuildException("EULA must be accepted to run a Minecraft server. " +
                "Check https://aka.ms/MinecraftEULA for more information")
    }

}

enum class Difficulty {
    PEACEFUL, EASY, NORMAL, HARD
}

enum class Gamemode {
    SURVIVAL, CREATIVE, SPECTATOR, ADVENTURE

}
