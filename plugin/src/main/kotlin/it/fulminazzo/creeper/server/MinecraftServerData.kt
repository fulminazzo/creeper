package it.fulminazzo.creeper.server

import it.fulminazzo.creeper.server.config.MinecraftServerConfig
import it.fulminazzo.creeper.server.config.MinecraftServerConfigBuilder

/**
 * Holds all the server data needed to run a Minecraft server.
 *
 * @constructor Creates a new Minecraft server data
 *
 * @param type the type of server
 * @param version the version of the server
 * @param config the server configuration
 * @param whitelist the whitelist of the server
 */
class MinecraftServerData(
    type: ServerType.MinecraftType,
    version: String,
    config: MinecraftServerConfig,
    whitelist: Set<String>
) : ServerData<ServerType.MinecraftType, MinecraftServerConfig>(
    type,
    version,
    config,
    whitelist
)

/**
 * Builder for [MinecraftServerData].
 *
 * @constructor Creates a new Minecraft server data builder
 */
class MinecraftServerDataBuilder : ServerDataBuilder<ServerType.MinecraftType, MinecraftServerConfigBuilder, MinecraftServerConfig>() {
    override val serverConfigBuilder: MinecraftServerConfigBuilder = MinecraftServerConfigBuilder()

    override fun build(): ServerData<ServerType.MinecraftType, MinecraftServerConfig> {
        return MinecraftServerData(
            type,
            version,
            serverConfigBuilder.build(),
            whitelist
        )
    }

}