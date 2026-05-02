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
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.time.Duration

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

    private var completeBoot: CompletableFuture<Unit> = CompletableFuture()

    /**
     * Checks if the specified line is the line indicating the end of the boot process.
     *
     * @param line the line to check
     * @return `true` if the server has completed the boot process, `false` otherwise
     */
    protected abstract fun isBootCompleteLine(line: String): Boolean

    /**
     * Assuming the server has been started, awaits that the boot process has been completed.
     *
     * @param timeout the maximum time to wait for the boot process to complete
     * @return a [CompletableFuture] that completes when the boot process is complete
     */
    fun awaitCompleteBoot(timeout: Duration): CompletableFuture<*> =
        completeBoot.orTimeout(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)

    /**
     * Starts the server using the specified settings.
     *
     * @return the PID of the process
     */
    fun start(): Long {
        check(!isRunning()) { "Server is already running" }

        checkJavaVersion()

        val command = mutableListOf(javaExecutable)
        specification.settings.flags.takeIf { it.isNotEmpty() }?.let { command += it.split(" ") }
        command += "-jar"
        command += executableName
        command += "nogui"

        logger.info("Starting server ${specification.id} on port ${specification.settings.port}...")
        lines.clear()

        shutdownHook = Thread { if (isRunning()) forceStop() }
        Runtime.getRuntime().addShutdownHook(shutdownHook)

        val proc = ProcessBuilder(command)
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start()
        process = proc

        completeBoot = CompletableFuture<Unit>()
        reader = CompletableFuture.runAsync({
            if (proc.isAlive) {
                proc.inputStream?.bufferedReader()?.forEachLine {
                    lines.add(it)
                    if (!completeBoot.isDone && isBootCompleteLine(it)) completeBoot.complete(Unit)
                }
            } else {
                val value = proc.exitValue()
                println(value)
                if (value != 0) {
                    logger.error("Server ${specification.id} failed: execution terminated with exit code $value")
                    printErrorLogs()
                    forceStop()
                }
            }
        }, executor).exceptionally { e ->
            logger.error("Exception while reading output of server ${specification.id}: ${e.message}")
            printErrorLogs()
            forceStop()
            null
        }

        return proc.pid()
    }

    /**
     * Forcefully stops the server.
     */
    fun forceStop() {
        if (isRunning()) logger.info("Forcefully stopping ${specification.id}...")
        shutdownHook?.let {
            try {
                Runtime.getRuntime().removeShutdownHook(it)
                shutdownHook = null
            } catch (_: IllegalStateException) {
                // JVM already removing it
            }
        }
        process?.destroy()
        completeBoot.cancel(true)
        reader?.cancel(true)
    }

    /**
     * Checks if the latest execution was successful
     *
     * @return `true` if it was
     */
    fun wasExecutionSuccessful(): Boolean = process?.exitValue() == 0

    /**
     * Checks if the server is currently running.
     *
     * @return `true` if the server is running, `false` otherwise
     */
    fun isRunning() = process?.isAlive ?: false

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

    private fun printErrorLogs() {
        logger.error("Logs to debug the problem:")
        lines.forEach { line -> logger.error("$line") }
    }

    private companion object {
        val javaExecutable = "${System.getProperty("java.home")}/bin/java"
        val currentVersion = VersionUtils.getJavaVersion(System.getProperty("java.version"))

    }

}