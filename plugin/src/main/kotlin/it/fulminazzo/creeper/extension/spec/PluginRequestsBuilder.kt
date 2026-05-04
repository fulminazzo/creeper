package it.fulminazzo.creeper.extension.spec

import it.fulminazzo.creeper.provider.plugin.*
import java.net.URI
import java.nio.file.Path

/**
 * A builder for many [PluginRequest]s.
 *
 * @constructor Creates a new requests builder
 */
abstract class PluginRequestsBuilder {
    internal val requests: MutableList<PluginRequest> = mutableListOf()

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
    fun url(url: URI) = url(url.toString())

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
    @JvmOverloads
    fun local(filepath: String, overwrite: Boolean = true) = local(Path.of(filepath), overwrite)

    /**
     * The plugin will be copied from the specified file.
     *
     * @param filepath the path of the file
     * @param overwrite if the file should be overwritten if it already exists
     */
    @JvmOverloads
    fun local(filepath: Path, overwrite: Boolean = true) {
        requests += LocalPluginRequest(filepath, overwrite)
    }

}