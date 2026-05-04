package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.download.Downloader
import it.fulminazzo.creeper.download.UnrecognizedStatusCodeException
import it.fulminazzo.creeper.util.sha256
import org.gradle.api.GradleException
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

    override fun getName(request: HttpPluginRequest): String = executeSafeRequest(request) {
        downloader.getFileName(request.url) ?: throw NullPointerException()
    }

    override fun handleRequest(request: HttpPluginRequest, directory: Path, filename: String): Path {
        logger.lifecycle("Downloading plugin from ${request.url}")
        return executeSafeRequest(request) {
            val destination = directory.resolve(filename)
            downloader.download(request.url, destination)
            destination
        }
    }

    private fun <T> executeSafeRequest(request: HttpPluginRequest, block: () -> T): T = try {
        block()
    } catch (e: Exception) {
        throw GradleException(
            "Could not find plugin from url: ${request.url}${
                e.message.takeIf { it != null }?.let { " ($it)" } ?: ""
            }")
    }

}

/**
 * Plugin request for [HttpPluginProvider].
 *
 * @property url the url to download the plugin from
 * @constructor Creates a new Http plugin request
 */
data class HttpPluginRequest(val url: String) : PluginRequest {

    override fun toHashString(): String = url.sha256()

}