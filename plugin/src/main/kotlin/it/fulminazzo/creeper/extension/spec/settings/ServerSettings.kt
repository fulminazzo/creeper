package it.fulminazzo.creeper.extension.spec.settings

import it.fulminazzo.creeper.util.MemorySize
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

/**
 * Holds all the settings for a general server to run.
 *
 * @property port the port the server should run on
 * @property players the maximum number of players allowed
 * @property flags the JVM flags to use
 * @constructor Creates a new Server config
 */
sealed class ServerSettings(val port: Int, val players: Int, val flags: String)

/**
 * Builder for [ServerSettings].
 *
 * @constructor Creates a new Server settings builder
 */
abstract class ServerSettingsBuilder : RamConfigurator {

    abstract val port: Property<Int>

    abstract val maximumPlayers: Property<Int>

    @get:Nested
    abstract val flags: JvmFlagsBuilder

    override val minimumRam: Property<MemorySize> get() = flags.minimumRam

    override val maximumRam: Property<MemorySize> get() = flags.maximumRam

    /**
     * Builds the server settings.
     *
     * @return the server settings
     * @throws GradleException if the configuration is invalid
     */
    abstract fun build(): ServerSettings

    /**
     * Applies the configuration to the JVM flags builder.
     *
     * @param action the configuration
     */
    fun flags(action: Action<JvmFlagsBuilder>) = action.execute(flags)

    /**
     * Gets the set port.
     *
     * @return the port
     */
    protected fun getSetPort(): Int {
        val p = port.getOrElse(25565)
        return p.takeIf { it in 1..65535 }
            ?: throw GradleException("Invalid port = $p, must be between 1 and 65535")
    }

    /**
     * Gets the set number of maximum players.
     *
     * @return the players
     */
    protected fun getSetPlayers(): Int =
        requirePositive(maximumPlayers.getOrElse(20), "maximumPlayers")

}