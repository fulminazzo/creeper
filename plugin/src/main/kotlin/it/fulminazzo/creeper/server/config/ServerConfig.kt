package it.fulminazzo.creeper.server.config

import it.fulminazzo.creeper.server.BuildException
import it.fulminazzo.creeper.server.requirePort
import it.fulminazzo.creeper.server.requirePositive

/**
 * General server configuration.
 *
 * @property eula if `false` the server will not start
 * @property port the port the server should run on
 * @property players the maximum number of players allowed
 * @property whitelist if `true` only players on the whitelist can join
 * @property flags the JVM flags to use
 * @constructor Creates a new Server config
 */
sealed class ServerConfig(
    val eula: Boolean,
    val port: Int,
    val players: Int,
    val whitelist: Boolean,
    val flags: String
)

/**
 * Builder for [ServerConfig].
 *
 * @constructor Creates a new Server config builder
 */
sealed class ServerConfigBuilder {
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
     * Builds the server config.
     *
     * @return the server config
     * @throws BuildException if the configuration is invalid
     */
    abstract fun build(): ServerConfig

    /**
     * Applies the given configuration to the JVM flags builder.
     *
     * @param configuration the configuration
     * @receiver the JVM flags builder
     */
    fun flags(configuration: JvmFlagsBuilder.() -> Unit) {
        flags.apply(configuration)
    }

}
