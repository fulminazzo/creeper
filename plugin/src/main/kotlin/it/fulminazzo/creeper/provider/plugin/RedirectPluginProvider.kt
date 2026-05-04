package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.gradle.api.logging.Logger
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

    override fun getName(request: PluginRequest): String =
        when (request) {
            is ModrinthPluginRequest -> modrinthPluginProvider.getName(request)
            is GitHubPluginRequest -> gitHubPluginProvider.getName(request)
            is HttpPluginRequest -> httpPluginProvider.getName(request)
            is LocalPluginRequest -> localPluginProvider.getName(request)
        }

    override fun handleRequest(request: PluginRequest, directory: Path, filename: String): Path =
        when (request) {
            is ModrinthPluginRequest -> modrinthPluginProvider.handleRequest(request, directory)
            is GitHubPluginRequest -> gitHubPluginProvider.handleRequest(request, directory)
            is HttpPluginRequest -> httpPluginProvider.handleRequest(request, directory)
            is LocalPluginRequest -> localPluginProvider.handleRequest(request, directory)
        }

}