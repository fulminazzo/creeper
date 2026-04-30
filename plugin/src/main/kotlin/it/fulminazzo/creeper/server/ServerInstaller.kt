package it.fulminazzo.creeper.server

import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.provider.MinecraftConfigProvider
import it.fulminazzo.creeper.server.spec.ServerSpec
import it.fulminazzo.creeper.server.spec.settings.ServerSettings
import org.gradle.api.logging.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * Generic installer for a server.
 *
 * @param T the type of the server platform
 * @param C the type of the server settings
 * @param S the type of the server specification
 * @property specification the specification of the server to install
 * @property jarProvider the provider of the server jar
 * @property configProvider the provider of the server configurations
 * @property logger the logger to use for logging
 * @constructor Creates a new Server installer
 */
class ServerInstaller<T : ServerType, C : ServerSettings, S : ServerSpec<T, C>>(
    private val specification: S,
    private val jarProvider: JarProvider<T>,
    private val configProvider: MinecraftConfigProvider,
    private val logger: Logger
) {

    /**
     * Installs the server jar in the given directory.
     *
     * @param directory the working directory where the server will be installed
     * @return the path of the installed server jar
     */
    internal fun installJar(directory: Path): CompletableFuture<Path> {
        val serverDirectory = getServerDirectory(directory)
        logger.info("Installing server ${specification.type} ${specification.version} in: $serverDirectory")
        return jarProvider.get(
            specification.type,
            specification.version,
            serverDirectory
        )
    }

    private fun getServerDirectory(parent: Path) =
        parent.resolve("${specification.type.name.lowercase()}-${specification.version}")

}