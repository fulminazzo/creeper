package it.fulminazzo.creeper.server

import it.fulminazzo.creeper.server.config.ServerConfig
import it.fulminazzo.creeper.server.config.ServerConfigBuilder

/**
 * Holds all the server data needed to run a server.
 *
 * @param T the type of server platform
 * @param C the type of server configuration
 * @property type the type of server
 * @property version the version of the server
 * @property config the server configuration
 * @property whitelist the whitelist of the server
 * @constructor Creates a new Server data
 */
sealed class ServerData<T : ServerType, C : ServerConfig>(
    val type: T,
    val version: String,
    val config: C,
    val whitelist: Set<String>
)

/**
 * Builder for [ServerData].
 *
 * @param T the type of server platform
 * @param B the type of server configuration builder
 * @param C the type of server configuration
 * @constructor Creates a new Server data builder
 */
sealed class ServerDataBuilder<T : ServerType, B : ServerConfigBuilder, C : ServerConfig> {
    private var _type: T? = null
    var type: T
        get() = _type ?: throw BuildException("Server type is required but was not set")
        set(value) {
            _type = value
        }
    
    private var _version: String? = null
    var version: String
        get() = _version ?: throw BuildException("Server version is required but was not set")
        set(value) {
            _version = value
        }

    protected val whitelist: MutableSet<String> = mutableSetOf()

    protected abstract val serverConfigBuilder: B

    /**
     * Builds the server data.
     *
     * @return the server data
     */
    abstract fun build(): ServerData<T, C>

    /**
     * Applies the given configuration to the server config builder.
     *
     * @param configuration the configuration
     */
    fun serverConfig(configuration: B.() -> Unit) {
        serverConfigBuilder.apply(configuration)
    }

    /**
     * Adds a player to the whitelist.
     *
     * @param name the name of the player
     */
    fun whitelist(name: String) {
        whitelist += name
    }

}
