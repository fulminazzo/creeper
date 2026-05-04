package it.fulminazzo.creeper.extension.spec

import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettingsBuilder
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.ServerType
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
abstract class MinecraftServerSpecBuilder :
    ServerSpecBuilder<ServerType.MinecraftType, MinecraftServerSettingsBuilder, MinecraftServerSettings>() {

    @get:Nested
    abstract override val serverConfig: MinecraftServerSettingsBuilder

    abstract val whitelist: SetProperty<String>

    abstract val operators: SetProperty<String>

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
    fun op(vararg names: String) = operator(*names)

    /**
     * Adds players to the operators' list.
     *
     * @param names the names of the players to add
     */
    fun operator(vararg names: String) = operators.addAll(names.toSet())

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