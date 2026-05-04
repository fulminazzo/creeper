package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.Hashable
import org.gradle.api.logging.Logger
import java.io.Serializable
import java.nio.file.Path

/**
 * A provider for server plugins.
 *
 * @param R the type of supported plugin requests
 * @param logger the logger to use for logging
 * @constructor Creates a new Plugin provider
 */
sealed class PluginProvider<R : PluginRequest>(protected val logger: Logger) {

    /**
     * Gets the file name of the plugin.
     *
     * @param request the plugin request
     * @return the file name
     */
    abstract fun getName(request: R): String

    /**
     * Handles the specified plugin request.
     *
     * @param directory the directory to download plugins to
     * @param request the plugin request
     * @param filename the name of the plugin file
     * @return the path of the downloaded plugin
     * @throws PluginNotFoundException if the plugin was not found
     */
    abstract fun handleRequest(request: R, directory: Path, filename: String): Path

    /**
     * Handles the specified plugin request.
     *
     * @param directory the directory to download plugins to
     * @param request the plugin request
     * @return the path of the downloaded plugin
     * @throws PluginNotFoundException if the plugin was not found
     */
    fun handleRequest(request: R, directory: Path): Path = handleRequest(request, directory, getName(request))

}

/**
 * Marker interface for plugin requests.
 */
sealed interface PluginRequest : Serializable, Hashable

/**
 * Exception thrown by a failed [PluginProvider.handleRequest].
 *
 * @constructor Creates a new Plugin not found exception
 *
 * @param message the exception message
 */
class PluginNotFoundException(message: String) : Exception(message)