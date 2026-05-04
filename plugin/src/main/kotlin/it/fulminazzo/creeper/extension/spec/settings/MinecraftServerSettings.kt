package it.fulminazzo.creeper.extension.spec.settings

import org.gradle.api.GradleException
import org.gradle.api.provider.Property

/**
 * Holds all the settings for a Minecraft server to run.
 *
 * @property eula if `false` the server will not start
 * @property whitelist if `true` only players on the whitelist can join
 * @property difficulty the difficulty of the server
 * @property gamemode the default gamemode of the server
 * @property generateStructures if `true` the server will generate structures
 * @property onlineMode if `true` only players with a premium account will be able to join
 * @property spawnProtection the destroy/build protection radius of the server
 * @property viewDistance the view distance of the server
 * @property simulationDistance the simulation distance of the server
 * @constructor Creates a new Minecraft server config
 *
 * @param port the port the server should run on
 * @param players the maximum number of players allowed
 * @param flags the JVM flags to use
 */
class MinecraftServerSettings(
    port: Int,
    players: Int,
    flags: String,
    val eula: Boolean,
    val whitelist: Boolean,
    val hardcore: Boolean,
    val difficulty: Difficulty,
    val gamemode: Gamemode,
    val generateStructures: Boolean,
    val onlineMode: Boolean,
    val spawnProtection: Int,
    val viewDistance: Int,
    val simulationDistance: Int
) : ServerSettings(
    port,
    players,
    flags
)

/**
 * Builder for [MinecraftServerSettings].
 *
 * @constructor Create a new Minecraft server settings builder
 */
abstract class MinecraftServerSettingsBuilder : ServerSettingsBuilder() {
    abstract val eula: Property<Boolean>
    abstract val whitelist: Property<Boolean>
    abstract val hardcore: Property<Boolean>
    abstract val difficulty: Property<Difficulty>
    abstract val gamemode: Property<Gamemode>
    abstract val generateStructures: Property<Boolean>
    abstract val onlineMode: Property<Boolean>
    abstract val spawnProtection: Property<Int>
    abstract val viewDistance: Property<Int>
    abstract val simulationDistance: Property<Int>

    init {
        eula.convention(false)
        whitelist.convention(false)
        hardcore.convention(false)
        difficulty.convention(Difficulty.PEACEFUL)
        gamemode.convention(Gamemode.SURVIVAL)
        generateStructures.convention(false)
        onlineMode.convention(false)
        spawnProtection.convention(0)
        viewDistance.convention(2)
        simulationDistance.convention(2)
        flags.aikars()
    }

    override fun build(): MinecraftServerSettings {
        return if (eula.get()) {
            MinecraftServerSettings(
                getSetPort(),
                getSetPlayers(),
                flags.build(),
                true,
                whitelist.get(),
                hardcore.get(),
                difficulty.get(),
                gamemode.get(),
                generateStructures.get(),
                onlineMode.get(),
                getSpawnProtection(),
                getViewDistance(),
                getSimulationDistance()
            )
        } else throw GradleException(
            "EULA must be accepted to run a Minecraft server. Check https://aka.ms/MinecraftEULA for more information"
        )
    }

    private fun getSpawnProtection(): Int = requireNatural(spawnProtection.get(), "spawnProtection")

    private fun getViewDistance(): Int = requirePositive(viewDistance.get(), "viewDistance")

    private fun getSimulationDistance(): Int = requirePositive(simulationDistance.get(), "simulationDistance")

}

enum class Difficulty {
    PEACEFUL, EASY, NORMAL, HARD
}

enum class Gamemode {
    SURVIVAL, CREATIVE, SPECTATOR, ADVENTURE
}