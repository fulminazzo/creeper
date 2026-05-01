package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.Downloader
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Implementation of [PluginProvider] that will download plugins from the web.
 *
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new Http plugin provider
 *
 * @param directory the directory to download plugins to
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 */
class HttpPluginProvider internal constructor(
    directory: Path,
    logger: Logger,
    executor: Executor,
    private val downloader: Downloader
) : PluginProvider<HttpPluginRequest>(
    directory,
    logger,
    executor
) {

    override fun handleRequest(request: HttpPluginRequest): CompletableFuture<Path> = CompletableFuture.supplyAsync({
        logger.info("Downloading plugin from ${request.url}")
        downloader.downloadIn(request.url, directory)
    }, executor)

}

/**
 * Plugin request for [HttpPluginProvider].
 *
 * @property url the url to download the plugin from
 * @constructor Creates a new Http plugin request
 */
data class HttpPluginRequest(val url: String) : PluginRequest