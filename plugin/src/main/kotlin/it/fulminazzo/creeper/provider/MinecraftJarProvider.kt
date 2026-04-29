package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ServerType
import java.nio.file.Path

/**
 * A provider for the executable of the specified Minecraft version.
 */
interface MinecraftJarProvider {

    /**
     * Gets the executable of the specified Minecraft version.
     *
     * @param platform the platform
     * @param version the version
     * @param directory the directory where the executable will be stored
     * @throws JarNotFoundException if the executable was not found
     */
    fun get(platform: ServerType.MinecraftType, version: String, directory: Path)

}