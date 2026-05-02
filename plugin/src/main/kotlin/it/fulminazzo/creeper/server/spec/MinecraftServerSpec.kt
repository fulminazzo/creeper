package it.fulminazzo.creeper.server.spec

import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.server.spec.settings.MinecraftServerSettingsBuilder

/**
 * Identifies the specification of a Minecraft server to run.
 *
 * @constructor Creates a new Minecraft server spec
 *
 * @param type the type of server
 * @param version the version of the server
 * @param config the server configuration
 * @param whitelist the whitelist of the server
 * @param operators the operator players of the server
 * @property plugins the plugins to install
 */
class MinecraftServerSpec(
    type: ServerType.MinecraftType,
    version: String,
    config: MinecraftServerSettings,
    val whitelist: Set<String>,
    val operators: Set<String>,
    plugins: List<PluginRequest>
) : ServerSpec<ServerType.MinecraftType, MinecraftServerSettings>(
    type,
    version,
    config,
    plugins
)

/**
 * Builder for [MinecraftServerSpec].
 *
 * @constructor Creates a new Minecraft server spec builder
 */
class MinecraftServerSpecBuilder :
    ServerSpecBuilder<ServerType.MinecraftType, MinecraftServerSettingsBuilder, MinecraftServerSettings>() {
    override val serverConfigBuilder: MinecraftServerSettingsBuilder = MinecraftServerSettingsBuilder()

    private val whitelist: MutableSet<String> = mutableSetOf()
    private val operators: MutableSet<String> = mutableSetOf()

    /**
     * Adds a player to the whitelist.
     *
     * @param name the name of the player
     */
    fun whitelist(name: String) {
        whitelist += name
    }

    /**
     * Adds a player to the operators' list.
     *
     * @param name the name of the player
     */
    fun op(name: String) = operator(name)

    /**
     * Adds a player to the operators' list.
     *
     * @param name the name of the player
     */
    fun operator(name: String) {
        operators += name
    }

    override fun build(): MinecraftServerSpec {
        return MinecraftServerSpec(
            type,
            version,
            serverConfigBuilder.build(),
            whitelist,
            operators,
            plugins.requests
        )
    }

}