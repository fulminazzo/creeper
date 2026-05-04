package it.fulminazzo.creeper.server.spec

import it.fulminazzo.creeper.provider.plugin.*
import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.settings.ServerSettings
import it.fulminazzo.creeper.server.spec.settings.ServerSettingsBuilder
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

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
    val id: String
        get() = "${type.id}-$version"

}

/**
 * Builder for [ServerSpec].
 *
 * @param T the type of server platform
 * @param B the type of server settings builder
 * @param S the type of server settings
 * @constructor Creates a new Server spec builder
 */
sealed class ServerSpecBuilder<T : ServerType, B : ServerSettingsBuilder, S : ServerSettings> {
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

    protected val plugins: PluginRequestsBuilder = PluginRequestsBuilder()

    protected abstract val serverConfigBuilder: B

    /**
     * Builds the server specification.
     *
     * @return the server spec
     * @throws BuildException if the specification is invalid
     */
    abstract fun build(): ServerSpec<T, S>

    /**
     * Allows editing of the server settings to use when running the server.
     *
     * @param function the function to apply the changes
     * @receiver the builder to edit the server settings
     */
    fun serverConfig(function: B.() -> Unit) {
        serverConfigBuilder.apply(function)
    }

    /**
     * Allows adding plugins to the server.
     *
     * @param function the function to add the plugins
     * @receiver the builder to add the plugins
     */
    fun plugins(function: PluginRequestsBuilder.() -> Unit) {
        plugins.apply(function)
    }

}

/**
 * A builder for [PluginRequest]s.
 *
 * @constructor Creates a new Plugin requests builder
 */
class PluginRequestsBuilder {
    val requests: MutableList<PluginRequest> = mutableListOf()

    /**
     * The plugin will be downloaded from Modrinth API.
     *
     * @param projectName the name of the project in Modrinth
     * @param version the name or number of the version
     * @param filename the name of the plugin file
     */
    fun modrinth(projectName: String, version: String, filename: String) {
        requests += ModrinthPluginRequest(projectName, version, filename)
    }

    /**
     * The plugin will be downloaded from GitHub API.
     *
     * @param owner the owner of the repository
     * @param repository the name of the repository
     * @param release the tag of the release
     * @param filename the name of the plugin file
     */
    fun github(owner: String, repository: String, release: String, filename: String) {
        requests += GitHubPluginRequest(owner, repository, release, filename)
    }

    /**
     * The plugin will be downloaded from the specified URL.
     *
     * @param url the URL of the plugin
     */
    fun url(url: URI) {
        url(url.toString())
    }

    /**
     * The plugin will be downloaded from the specified URL.
     *
     * @param url the URL of the plugin
     */
    fun url(url: String) {
        requests += HttpPluginRequest(url)
    }

    /**
     * The plugin will be copied from the specified file.
     *
     * @param filepath the path of the file
     * @param overwrite if the file should be overwritten if it already exists
     */
    fun local(filepath: String, overwrite: Boolean = true) {
        local(Paths.get(filepath), overwrite)
    }

    /**
     * The plugin will be copied from the specified file.
     *
     * @param filepath the path of the file
     * @param overwrite if the file should be overwritten if it already exists
     */
    fun local(filepath: Path, overwrite: Boolean = true) {
        requests += LocalPluginRequest(filepath, overwrite)
    }

}
