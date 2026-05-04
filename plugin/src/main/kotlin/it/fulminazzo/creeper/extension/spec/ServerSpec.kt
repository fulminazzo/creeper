package it.fulminazzo.creeper.extension.spec

import it.fulminazzo.creeper.extension.spec.settings.ServerSettings
import it.fulminazzo.creeper.extension.spec.settings.ServerSettingsBuilder
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.ServerType
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

/**
 * Identifies the general specification of a server to run.
 *
 * @param T the type of server platform
 * @param S the type of server settings
 * @property type the type of server
 * @property version the version of the server
 * @property settings the server settings
 * @property plugins the plugins to install
 * @constructor Creates a new Server spec
 */
sealed class ServerSpec<T : ServerType, S : ServerSettings>(
    val type: T,
    val version: String,
    val settings: S,
    val plugins: List<PluginRequest>
) {
    val id: String = "${type.id}-$version"

}

/**
 * Builder for [ServerSpec].
 *
 * @param T the type of server platform
 * @param B the type of server settings builder
 * @param S the type of server settings
 * @constructor Creates a new Server spec builder
 */
abstract class ServerSpecBuilder<T : ServerType, B : ServerSettingsBuilder, S : ServerSettings> {

    abstract val type: Property<String>

    abstract val version: Property<String>

    @get:Nested
    abstract val plugins: PluginRequestsBuilder

    abstract val serverConfig: B

    protected abstract val serverClassType: Class<T>

    /**
     * Builds the server specification.
     *
     * @return the server spec
     * @throws GradleException if the specification is invalid
     */
    abstract fun build(): ServerSpec<T, S>

    /**
     * Applies the configuration to the server configuration builder.
     *
     * @param action the configuration
     */
    fun serverConfig(action: Action<B>) = action.execute(serverConfig)

    /**
     * Applies the configuration to the plugin requests builder.
     *
     * @param action the configuration
     */
    fun plugins(action: Action<PluginRequestsBuilder>) = action.execute(plugins)

    /**
     * Gets the set type.
     *
     * @return the type
     */
    protected fun getSetType(): T {
        val raw = type.orNull ?: throw GradleException("Invalid server configuration, missing: type =")
        val type =
            ServerType.valueOf(raw) ?: throw GradleException("Invalid server configuration, unrecognized: type = $raw")
        return type.takeIf { serverClassType.isInstance(it) }?.let {
            @Suppress("UNCHECKED_CAST")
            it as T
        } ?: throw GradleException(
            "Invalid server configuration, type = $raw is not applicable to this configuration. " +
                    "Expected ${serverClassType.simpleName}"
        )
    }

    /**
     * Gets the set version.
     *
     * @return the version
     */
    protected fun getSetVersion(): String =
        version.orNull ?: throw GradleException("Invalid server configuration, missing: version =")

}