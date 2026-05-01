package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * A special [PluginProvider] that redirects requests to the most appropriate provider.
 *
 * @constructor Creates a new Redirect plugin provider
 *
 * @param directory the directory to download plugins to
 * @param logger the logger to use for logging
 * @param downloader the downloader to use for downloading the plugins
 */
class RedirectPluginProvider(
    directory: Path,
    logger: Logger,
    downloader: CachedDownloader
) : PluginProvider<PluginRequest>(
    directory,
    logger
) {
    private val gitHubPluginProvider = GitHubPluginProvider(directory, logger, downloader)
    private val httpPluginProvider = HttpPluginProvider(directory, logger, Downloader.http())
    private val localPluginProvider = LocalPluginProvider(directory, logger)

    override fun handleRequest(request: PluginRequest): CompletableFuture<Path> =
        when (request) {
            is GitHubPluginRequest -> gitHubPluginProvider.handleRequest(request)
            is HttpPluginRequest -> httpPluginProvider.handleRequest(request)
            is LocalPluginRequest -> localPluginProvider.handleRequest(request)
        }

}