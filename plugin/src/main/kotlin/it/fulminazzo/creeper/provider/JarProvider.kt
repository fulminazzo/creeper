package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.server.ServerType
import java.nio.file.Path

/**
 * A provider for the executable of the specified server platform and version.
 */
sealed interface JarProvider {

    /**
     * Downloads the executable of the specified server platform and version to the given directory.
     *
     * @param platform the platform
     * @param version the version
     * @param directory the directory where the executable will be stored
     * @return the path of the downloaded executable
     * @throws JarNotFoundException if the executable was not found
     */
    fun get(platform: ServerType, version: String, directory: Path): Path

}