package it.fulminazzo.creeper.server.spec

import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.settings.ServerSettings
import it.fulminazzo.creeper.server.spec.settings.ServerSettingsBuilder

/**
 * Identifies the general specification of a server to run.
 *
 * @param T the type of server platform
 * @param C the type of server configuration
 * @property type the type of server
 * @property version the version of the server
 * @property config the server configuration
 * @property whitelist the whitelist of the server
 * @constructor Creates a new Server spec
 */
sealed class ServerSpec<T : ServerType, C : ServerSettings>(
    val type: T,
    val version: String,
    val config: C,
    val whitelist: Set<String>
)

/**
 * Builder for [ServerSpec].
 *
 * @param T the type of server platform
 * @param B the type of server configuration builder
 * @param C the type of server configuration
 * @constructor Creates a new Server spec builder
 */
sealed class ServerSpecBuilder<T : ServerType, B : ServerSettingsBuilder, C : ServerSettings> {
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
     * Builds the server specification.
     *
     * @return the server spec
     */
    abstract fun build(): ServerSpec<T, C>

    /**
     * Allows editing of the server settings to use when running the server.
     *
     * @param configuration the function to apply the changes
     * @receiver the builder to edit the server settings
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
