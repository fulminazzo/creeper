package it.fulminazzo.creeper.server.spec

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
 */
class MinecraftServerSpec(
    type: ServerType.MinecraftType,
    version: String,
    config: MinecraftServerSettings,
    whitelist: Set<String>
) : ServerSpec<ServerType.MinecraftType, MinecraftServerSettings>(
    type,
    version,
    config,
    whitelist
)

/**
 * Builder for [MinecraftServerSpec].
 *
 * @constructor Creates a new Minecraft server spec builder
 */
class MinecraftServerSpecBuilder :
    ServerSpecBuilder<ServerType.MinecraftType, MinecraftServerSettingsBuilder, MinecraftServerSettings>() {
    override val serverConfigBuilder: MinecraftServerSettingsBuilder = MinecraftServerSettingsBuilder()

    override fun build(): ServerSpec<ServerType.MinecraftType, MinecraftServerSettings> {
        return MinecraftServerSpec(
            type,
            version,
            serverConfigBuilder.build(),
            whitelist
        )
    }

}