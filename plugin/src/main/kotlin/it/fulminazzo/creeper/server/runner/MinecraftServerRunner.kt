package it.fulminazzo.creeper.server.runner

import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings
import org.gradle.api.logging.Logger
import java.nio.file.Path
import java.util.concurrent.Executor

/**
 * Special implementation of [MinecraftServerRunner] for Minecraft servers.
 *
 * @constructor Creates a new Minecraft server installer
 *
 * @param specification the specification of the server to install
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 * @param directory the directory where the server files are stored
 * @param javaExecutable the path to the Java executable
 */
class MinecraftServerRunner(
    specification: MinecraftServerSpec,
    logger: Logger,
    executor: Executor,
    directory: Path,
    javaExecutable: String
) : ServerRunner<ServerType.MinecraftType, MinecraftServerSettings, MinecraftServerSpec>(
    specification,
    logger,
    executor,
    directory,
    javaExecutable
) {

    override fun isBootCompleteLine(line: String): Boolean = COMPLETE_LINE_PATTERN.matches(line)

    private companion object {
        val COMPLETE_LINE_PATTERN =
            ".*\\[[0-9]{2}:[0-9]{2}:[0-9]{2}[^]]*INFO[^]]*]:.*Done \\([0-9]+\\.[0-9]+s\\)!.*".toRegex()
    }

}