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
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 * @param downloader the downloader to use for downloading the plugins
 */
class RedirectPluginProvider(
    logger: Logger,
    executor: Executor,
    downloader: CachedDownloader
) : PluginProvider<PluginRequest>(
    logger,
    executor
) {
    private val modrinthPluginProvider = ModrinthPluginProvider(logger, executor, downloader)
    private val gitHubPluginProvider = GitHubPluginProvider(logger, executor, downloader)
    private val httpPluginProvider = HttpPluginProvider(logger, executor, Downloader.http())
    private val localPluginProvider = LocalPluginProvider(logger, executor)

    override fun handleRequest(directory: Path, request: PluginRequest): CompletableFuture<Path> =
        when (request) {
            is ModrinthPluginRequest -> modrinthPluginProvider.handleRequest(directory, request)
            is GitHubPluginRequest -> gitHubPluginProvider.handleRequest(directory, request)
            is HttpPluginRequest -> httpPluginProvider.handleRequest(directory, request)
            is LocalPluginRequest -> localPluginProvider.handleRequest(directory, request)
        }

}