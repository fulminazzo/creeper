package it.fulminazzo.creeper.extension.spec

import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettingsBuilder
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.ServerType
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested

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
class MinecraftServerSpec @JvmOverloads constructor(
    type: ServerType.MinecraftType = ServerType.VANILLA,
    version: String = "1.21.11",
    config: MinecraftServerSettings = MinecraftServerSettings(),
    val whitelist: Set<String> = emptySet(),
    val operators: Set<String> = emptySet(),
    plugins: List<PluginRequest> = emptyList()
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
abstract class MinecraftServerSpecBuilder :
    ServerSpecBuilder<ServerType.MinecraftType, MinecraftServerSettingsBuilder, MinecraftServerSettings>() {

    abstract override val type: Property<String>

    @get:Nested
    abstract override val serverConfig: MinecraftServerSettingsBuilder

    abstract val whitelist: SetProperty<String>

    abstract val operators: SetProperty<String>

    override val serverClassType: Class<ServerType.MinecraftType> = ServerType.MinecraftType::class.java

    /**
     * Adds players to the whitelist.
     *
     * @param names the names of the players to add
     */
    fun whitelist(vararg names: String) = whitelist.addAll(names.toSet())

    /**
     * Adds players to the operators' list.
     *
     * @param names the names of the players to add
     */
    fun ops(vararg names: String) = operators(*names)

    /**
     * Adds players to the operators' list.
     *
     * @param names the names of the players to add
     */
    fun operators(vararg names: String) = operators.addAll(names.toSet())

    override fun build(): MinecraftServerSpec {
        return MinecraftServerSpec(
            getSetType(),
            getSetVersion(),
            serverConfig.build(),
            getSetWhitelist(),
            getSetOperators(),
            plugins.requests
        )
    }

    private fun getSetWhitelist(): Set<String> = whitelist.getOrElse(emptySet())

    private fun getSetOperators(): Set<String> = operators.getOrElse(emptySet())

}