package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.Downloader
import org.gradle.api.logging.Logger
import java.nio.file.Path

/**
 * Implementation of [PluginProvider] that will download plugins from the web.
 *
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new Http plugin provider
 *
 * @param logger the logger to use for logging
 */
class HttpPluginProvider internal constructor(
    logger: Logger,
    private val downloader: Downloader
) : PluginProvider<HttpPluginRequest>(logger) {

    override fun handleRequest(directory: Path, request: HttpPluginRequest): Path {
        logger.info("Downloading plugin from ${request.url}")
        return downloader.downloadIn(request.url, directory)
            ?: throw PluginNotFoundException("Could not find plugin from url: ${request.url}")
    }

}

/**
 * Plugin request for [HttpPluginProvider].
 *
 * @property url the url to download the plugin from
 * @constructor Creates a new Http plugin request
 */
data class HttpPluginRequest(val url: String) : PluginRequest