package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.CachedDownloader
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * Implementation of [PluginProvider] that will download plugins from their GitHub repositories.
 *
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new GitHub plugin provider
 *
 * @param directory the directory to download plugins to
 * @param logger the logger to use for logging
 */
class GitHubPluginProvider(
    directory: Path,
    logger: Logger,
    private val downloader: CachedDownloader
) : PluginProvider<GitHubPluginRequest>(
    directory,
    logger
) {

    override fun handleRequest(request: GitHubPluginRequest): CompletableFuture<Path> {
        TODO("Not yet implemented")
    }

}

/**
 * Plugin request for [GitHubPluginProvider].
 *
 * @property owner the owner of the repository
 * @property repository the name of the repository
 * @property release the tag of the release in the repository
 * @property name the name of the plugin file
 * @constructor Creates a new GitHub plugin request
 */
data class GitHubPluginRequest(
    val owner: String,
    val repository: String,
    val release: String,
    val name: String
) : PluginRequest