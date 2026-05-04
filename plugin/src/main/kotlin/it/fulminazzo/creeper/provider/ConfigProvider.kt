package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ServerType
import java.nio.file.Path

/**
 * A provider for configurations of the specified server platform and version.
 */
sealed interface ConfigProvider {

    /**
     * Downloads the configuration file of the specified server platform and version to the given directory.
     *
     * @param name the name of the configuration (with the extension)
     * @param platform the platform
     * @param version the version
     * @param directory the directory where the file will be stored
     * @return the path of the downloaded configuration file
     * @throws ConfigurationNotFoundException if the configuration was not found
     */
    fun get(name: String, platform: ServerType, version: String, directory: Path): Path

}