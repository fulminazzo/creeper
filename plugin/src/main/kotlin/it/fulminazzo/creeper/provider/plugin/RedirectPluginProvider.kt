package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * A special [PluginProvider] that redirects requests to the most appropriate provider.
 *
 * @constructor Creates a new Redirect plugin provider
 *
 * @param directory the directory to download plugins to
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 * @param downloader the downloader to use for downloading the plugins
 */
class RedirectPluginProvider(
    directory: Path,
    logger: Logger,
    executor: Executor,
    downloader: CachedDownloader
) : PluginProvider<PluginRequest>(
    directory,
    logger,
    executor
) {
    private val modrinthPluginProvider = ModrinthPluginProvider(directory, logger, executor, downloader)
    private val gitHubPluginProvider = GitHubPluginProvider(directory, logger, executor, downloader)
    private val httpPluginProvider = HttpPluginProvider(directory, logger, executor, Downloader.http())
    private val localPluginProvider = LocalPluginProvider(directory, logger, executor)

    override fun handleRequest(request: PluginRequest): CompletableFuture<Path> =
        when (request) {
            is ModrinthPluginRequest -> modrinthPluginProvider.handleRequest(request)
            is GitHubPluginRequest -> gitHubPluginProvider.handleRequest(request)
            is HttpPluginRequest -> httpPluginProvider.handleRequest(request)
            is LocalPluginRequest -> localPluginProvider.handleRequest(request)
        }

}