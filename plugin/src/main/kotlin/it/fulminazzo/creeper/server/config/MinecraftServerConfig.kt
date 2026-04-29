package it.fulminazzo.creeper.server.config

/**
 * Minecraft server configuration.
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
class MinecraftServerConfig(
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
) : ServerConfig(
    eula,
    port,
    players,
    whitelist,
    flags
)

/**
 * Builder for [MinecraftServerConfig].
 *
 * @constructor Create a new Minecraft server config builder
 */
class MinecraftServerConfigBuilder: ServerConfigBuilder() {
    var difficulty: Difficulty = Difficulty.PEACEFUL
    var gamemode: Gamemode = Gamemode.SURVIVAL
    var generateStructures: Boolean = false
    var onlineMode: Boolean = false
    var spawnProtection: Int = 0
    var viewDistance: Int = 2
    var simulationDistance: Int = 2

    init {
        flags.from(JvmFlagsBuilder.AKAIR_FLAGS)
    }

    override fun build(): MinecraftServerConfig {
        return MinecraftServerConfig(
            eula,
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
        )
    }

}

enum class Difficulty {
    PEACEFUL, EASY, NORMAL, HARD
}

enum class Gamemode {
    SURVIVAL, CREATIVE, SPECTATOR, ADVENTURE

}
