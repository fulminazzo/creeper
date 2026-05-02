package it.fulminazzo.creeper.server.runner

import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.ServerSpec
import it.fulminazzo.creeper.server.spec.settings.ServerSettings
import it.fulminazzo.creeper.util.VersionUtils
import org.slf4j.Logger
import java.nio.file.Path
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.io.path.exists
import kotlin.io.path.name

/**
 * Generic runner for a server.
 *
 * @param T the type of the server platform
 * @param C the type of the server settings
 * @param S the type of the server specification
 * @property specification the specification of the server to install
 * @property logger the logger to use for logging
 * @property executor the executor to use for asynchronous operations
 * @property directory the directory where the server files are stored
 * @constructor Creates a new Server runner
 */
sealed class ServerRunner<T : ServerType, C : ServerSettings, S : ServerSpec<T, C>>(
    protected val specification: S,
    protected val logger: Logger,
    protected val executor: Executor,
    protected val directory: Path
) {
    private val executableName: String
        get() {
            val serverId = specification.id
            val executable = directory.resolve("$serverId.jar")
            check(executable.exists()) { "Server $serverId executable not found in $directory" }
            return executable.name
        }

    private val lines = Collections.synchronizedList(mutableListOf<String>())

    private var shutdownHook: Thread? = null
    private var process: Process? = null
    private var reader: CompletableFuture<*>? = null

    /**
     * Starts the server using the specified settings.
     */
    fun start() {
        check(!isRunning()) { "Server is already running" }

        checkJavaVersion()

        val command = mutableListOf(javaExecutable)
        command += specification.settings.flags.split(" ")
        command += "-jar"
        command += executableName
        command += "nogui"

        logger.info("Starting server ${specification.id} on port ${specification.settings.port}...")
        lines.clear()
        shutdownHook = Thread { if (isRunning()) forceStop() }
        Runtime.getRuntime().addShutdownHook(shutdownHook)
        process = ProcessBuilder(command)
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start()
        reader = CompletableFuture.runAsync({
            process?.inputStream?.bufferedReader()?.forEachLine { lines.add(it) }
        }, executor)
    }

    /**
     * Forcefully stops the server.
     */
    fun forceStop() {
        check(isRunning()) { "Server is not running" }
        logger.info("Forcefully stopping ${specification.id}...")
        shutdownHook?.let {
            try {
                Runtime.getRuntime().removeShutdownHook(it)
                shutdownHook = null
            } catch (_: IllegalStateException) {
                // JVM already removing it
            }
        }
        process?.destroy()
        reader?.cancel(true)
    }

    /**
     * Checks if the current Java version is compatible with the required one to run the server.
     *
     * @throws IllegalStateException if the current Java version is not compatible
     */
    internal fun checkJavaVersion() {
        val version = specification.version
        val requiredVersion = VersionUtils.getJavaVersion(version)
        check(currentVersion >= requiredVersion) {
            "Minecraft ${specification.type.name} $version requires Java $requiredVersion or higher. Current Java version: $currentVersion"
        }
    }

    /**
     * Checks if the server is currently running.
     *
     * @return `true` if the server is running, `false` otherwise
     */
    fun isRunning() = process?.isAlive ?: false

    private companion object {
        val javaExecutable = "${System.getProperty("java.home")}/bin/java"
        val currentVersion = VersionUtils.getJavaVersion(System.getProperty("java.version"))

    }

}