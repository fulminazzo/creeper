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
 * @property plugins the plugins to install
 */
class MinecraftServerSpec(
    type: ServerType.MinecraftType,
    version: String,
    config: MinecraftServerSettings,
    val whitelist: Set<String>,
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

    /**
     * Adds a player to the whitelist.
     *
     * @param name the name of the player
     */
    fun whitelist(name: String) {
        whitelist += name
    }

    override fun build(): ServerSpec<ServerType.MinecraftType, MinecraftServerSettings> {
        return MinecraftServerSpec(
            type,
            version,
            serverConfigBuilder.build(),
            whitelist,
            plugins.requests
        )
    }

}