package it.fulminazzo.creeper.provider.plugin

import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * Implementation of [PluginProvider] that will get plugins from the file system and copy them to the desired directory.
 *
 * @constructor Creates a new Local plugin provider
 *
 * @param directory the directory to download plugins to
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 */
class LocalPluginProvider internal constructor(
    directory: Path,
    logger: Logger,
    executor: Executor
) : PluginProvider<LocalPluginRequest>(
    directory,
    logger,
    executor
) {

    override fun handleRequest(request: LocalPluginRequest): CompletableFuture<Path> = CompletableFuture.supplyAsync({
        val file = request.file
        if (!file.exists()) throw PluginNotFoundException("Could not find plugin from local file: $file")
        val destination = directory.createDirectories().resolve(file.fileName)
        if (!destination.exists() || request.overwrite) {
            logger.info("Copying plugin from $file to $destination")
            file.copyTo(destination, overwrite = true)
        }
        destination
    }, executor)

}

/**
 * Plugin request for [LocalPluginProvider].
 *
 * @property file the file to install
 * @property overwrite if `true`, it will overwrite the existing file
 * @constructor Creates a new Local plugin request
 */
data class LocalPluginRequest(val file: Path, val overwrite: Boolean) : PluginRequest