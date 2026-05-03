package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.slf4j.Logger
import java.nio.file.Path

/**
 * A special [PluginProvider] that redirects requests to the most appropriate provider.
 *
 * @constructor Creates a new Redirect plugin provider
 *
 * @param logger the logger to use for logging
 * @param downloader the downloader to use for downloading the plugins
 */
class RedirectPluginProvider(
    logger: Logger,
    downloader: CachedDownloader
) : PluginProvider<PluginRequest>(logger) {
    private val modrinthPluginProvider = ModrinthPluginProvider(logger, downloader)
    private val gitHubPluginProvider = GitHubPluginProvider(logger, downloader)
    private val httpPluginProvider = HttpPluginProvider(logger, Downloader.http())
    private val localPluginProvider = LocalPluginProvider(logger)

    override fun handleRequest(directory: Path, request: PluginRequest): Path =
        when (request) {
            is ModrinthPluginRequest -> modrinthPluginProvider.handleRequest(directory, request)
            is GitHubPluginRequest -> gitHubPluginProvider.handleRequest(directory, request)
            is HttpPluginRequest -> httpPluginProvider.handleRequest(directory, request)
            is LocalPluginRequest -> localPluginProvider.handleRequest(directory, request)
        }

}