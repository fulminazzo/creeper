package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.server.ServerType
import kotlinx.coroutines.Deferred
import java.nio.file.Path

/**
 * A provider for the executable of the specified Minecraft platform and version.
 */
interface MinecraftJarProvider {

    /**
     * Downloads the executable of the specified Minecraft platform and version to the given directory.
     *
     * @param platform the platform
     * @param version the version
     * @param directory the directory where the executable will be stored
     * @return the path of the downloaded executable
     * @throws JarNotFoundException if the executable was not found
     */
    fun get(platform: ServerType.MinecraftType, version: String, directory: Path): Deferred<Path>

}