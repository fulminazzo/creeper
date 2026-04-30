package it.fulminazzo.creeper.server.spec.settings

import it.fulminazzo.creeper.server.spec.BuildException
import it.fulminazzo.creeper.server.spec.requirePort
import it.fulminazzo.creeper.server.spec.requirePositive
import it.fulminazzo.creeper.util.MemorySize

/**
 * Holds all the settings for a general server to run.
 *
 * @property eula if `false` the server will not start
 * @property port the port the server should run on
 * @property players the maximum number of players allowed
 * @property whitelist if `true` only players on the whitelist can join
 * @property flags the JVM flags to use
 * @constructor Creates a new Server config
 */
sealed class ServerSettings(
    val eula: Boolean,
    val port: Int,
    val players: Int,
    val whitelist: Boolean,
    val flags: String
)

/**
 * Builder for [ServerSettings].
 *
 * @constructor Creates a new Server settings builder
 */
sealed class ServerSettingsBuilder {
    var eula: Boolean = false
    var port: Int = 25565
        set(value) {
            field = value.requirePort()
        }
    var players: Int = 20
        set(value) {
            field = value.requirePositive("players count")
        }
    var whitelist: Boolean = false
    protected val flags: JvmFlagsBuilder = JvmFlagsBuilder()

    var minimumRam: MemorySize by flags::minimumRam
    var maximumRam: MemorySize by flags::maximumRam

    /**
     * Builds the server settings.
     *
     * @return the server settings
     * @throws BuildException if the configuration is invalid
     */
    abstract fun build(): ServerSettings

    /**
     * Allows editing of the JVM flags to use when running the server.
     *
     * @param configuration the function to apply the changes
     * @receiver the builder to edit the JVM flags
     */
    fun flags(configuration: JvmFlagsBuilder.() -> Unit) {
        flags.apply(configuration)
    }

}
