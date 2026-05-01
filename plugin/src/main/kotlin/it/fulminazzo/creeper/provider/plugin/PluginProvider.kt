package it.fulminazzo.creeper.provider.plugin

import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * A provider for server plugins.
 *
 * @param R the type of supported plugin requests
 * @param directory the directory to download plugins to
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 * @constructor Creates a new Plugin provider
 */
sealed class PluginProvider<R : PluginRequest>(
    protected val directory: Path,
    protected val logger: Logger,
    protected val executor: Executor
) {

    /**
     * Handles the specified plugin request.
     *
     * @param request the plugin request
     * @return the path of the downloaded plugin
     */
    abstract fun handleRequest(request: R): CompletableFuture<Path>

}

/**
 * Marker interface for plugin requests.
 */
sealed interface PluginRequest