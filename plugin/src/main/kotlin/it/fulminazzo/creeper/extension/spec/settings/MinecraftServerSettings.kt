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
class MinecraftServerSettings @JvmOverloads constructor(
    port: Int = 25565,
    players: Int = 20,
    flags: String = "-Xms512M -Xmx2G",
    val eula: Boolean = false,
    val whitelist: Boolean = false,
    val hardcore: Boolean = false,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val gamemode: Gamemode = Gamemode.SURVIVAL,
    val generateStructures: Boolean = false,
    val onlineMode: Boolean = false,
    val spawnProtection: Int = 0,
    val viewDistance: Int = 2,
    val simulationDistance: Int = 2
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
    abstract val difficulty: Property<String>
    abstract val gamemode: Property<String>
    abstract val generateStructures: Property<Boolean>
    abstract val onlineMode: Property<Boolean>
    abstract val spawnProtection: Property<Int>
    abstract val viewDistance: Property<Int>
    abstract val simulationDistance: Property<Int>

    /**
     * PROPERTY VALUES GETTERS
     */
    private val difficultyValue: Difficulty
        get() = getEnumValue(Difficulty::class.java, "difficulty", difficulty)
    private val gamemodeValue: Gamemode
        get() = getEnumValue(Gamemode::class.java, "gamemode", gamemode)
    private val spawnProtectionValue: Int
        get() = requireNatural(spawnProtection.get(), "spawnProtection")
    private val viewDistanceValue: Int
        get() = requirePositive(viewDistance.get(), "viewDistance")
    private val simulationDistanceValue: Int
        get() = requirePositive(simulationDistance.get(), "simulationDistance")

    init {
        eula.convention(false)
        whitelist.convention(false)
        hardcore.convention(false)
        difficulty.convention(Difficulty.PEACEFUL.name)
        gamemode.convention(Gamemode.SURVIVAL.name)
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
                portValue,
                maximumPlayersValue,
                flagsValue,
                true,
                whitelist.get(),
                hardcore.get(),
                difficultyValue,
                gamemodeValue,
                generateStructures.get(),
                onlineMode.get(),
                spawnProtectionValue,
                viewDistanceValue,
                simulationDistanceValue
            )
        } else throw GradleException(
            "EULA must be accepted to run a Minecraft server. Check https://aka.ms/MinecraftEULA for more information"
        )
    }

}

enum class Difficulty {
    PEACEFUL, EASY, NORMAL, HARD
}

enum class Gamemode {
    SURVIVAL, CREATIVE, SPECTATOR, ADVENTURE
}