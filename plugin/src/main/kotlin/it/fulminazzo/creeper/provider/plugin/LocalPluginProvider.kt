package it.fulminazzo.creeper.provider.plugin

import org.gradle.api.logging.Logger
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * Implementation of [PluginProvider] that will get plugins from the file system and copy them to the desired directory.
 *
 * @constructor Creates a new Local plugin provider
 *
 * @param logger the logger to use for logging
 */
class LocalPluginProvider internal constructor(
    logger: Logger,
) : PluginProvider<LocalPluginRequest>(logger) {

    override fun handleRequest(directory: Path, request: LocalPluginRequest): Path {
        val file = request.file
        if (!file.exists()) throw PluginNotFoundException("Could not find plugin from local file: $file")
        val destination = directory.createDirectories().resolve(file.fileName)
        if (!destination.exists() || request.overwrite) {
            logger.info("Copying plugin from $file to $destination")
            file.copyTo(destination, overwrite = true)
        }
        return destination
    }

}

/**
 * Plugin request for [LocalPluginProvider].
 *
 * @property file the file to install
 * @property overwrite if `true`, it will overwrite the existing file
 * @constructor Creates a new Local plugin request
 */
data class LocalPluginRequest(val file: Path, val overwrite: Boolean) : PluginRequest