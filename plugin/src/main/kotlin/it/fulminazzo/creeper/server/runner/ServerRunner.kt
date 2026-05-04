package it.fulminazzo.creeper.server.runner

import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.extension.spec.settings.ServerSettings
import it.fulminazzo.creeper.server.ServerType
import org.gradle.api.logging.Logger
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
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
 * @property javaExecutable the path to the Java executable
 * @constructor Creates a new Server runner
 */
sealed class ServerRunner<T : ServerType, C : ServerSettings, S : ServerSpec<T, C>>(
    private val specification: S,
    private val logger: Logger,
    private val executor: Executor,
    private val directory: Path,
    private val javaExecutable: String
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
     * Awaits termination of the server process.
     *
     * @return the exit value
     */
    fun await(): Int {
        reader?.join()
        val exitValue = process?.waitFor()
        if (isExitValueInvalid(exitValue)) {
            logger.error(formatLog("Server exited with non-zero status code: $exitValue"))
            printLogsInError()
            handleAwaitCompleteBootNonZeroStatus(exitValue)
            forceStop()
        }
        return exitValue ?: SUCCESS
    }

    /**
     * Assuming the server has been started, awaits that the boot process has been completed.
     *
     * @param timeout the maximum time to wait for the boot process to complete
     * @return a [CompletableFuture] that completes when the boot process is complete
     */
    fun awaitCompleteBoot(timeout: Duration): CompletableFuture<*> =
        completeBoot.orTimeout(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .exceptionally { t ->
                if (t is TimeoutException) {
                    logger.error(formatLog("Server boot process timed out after ${timeout.inWholeSeconds} seconds"))
                    printLogsInError()
                }
                throw t
            }

    /**
     * Starts the server using the specified settings.
     *
     * @return the PID of the process
     */
    fun start(): Long {
        check(!isRunning()) { "Server is already running" }

        val command = mutableListOf(javaExecutable)
        specification.settings.flags.takeIf { it.isNotEmpty() }?.let { command += it.split(" ") }
        command += "-jar"
        command += executableName
        command += "nogui"

        logger.info(formatLog("Starting server on port ${specification.settings.port}..."))
        lines.clear()

        shutdownHook = Thread { if (isRunning()) forceStop() }
        Runtime.getRuntime().addShutdownHook(shutdownHook)

        process = ProcessBuilder(command)
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start()

        completeBoot = CompletableFuture<Unit>()
        reader = CompletableFuture.runAsync({
            process?.inputStream?.bufferedReader()?.forEachLine {
                lines.add(it)
                if (!completeBoot.isDone && isBootCompleteLine(it)) completeBoot.complete(Unit)
            }
            process?.waitFor()?.let {
                if (isExitValueInvalid(it))
                    handleAwaitCompleteBootNonZeroStatus(it)
            }
        }, executor)

        return process!!.pid()
    }

    /**
     * Forcefully stops the server.
     */
    fun forceStop() {
        if (isRunning()) logger.info(formatLog("Forcefully stopping server..."))
        shutdownHook?.let {
            try {
                Runtime.getRuntime().removeShutdownHook(it)
                shutdownHook = null
            } catch (_: IllegalStateException) {
                // JVM already removing it
            }
        }
        process?.destroyForcibly()?.waitFor(5, TimeUnit.SECONDS)
        completeBoot.cancel(true)
        reader?.join()
    }

    private fun handleAwaitCompleteBootNonZeroStatus(exitValue: Int?) {
        completeBoot.completeExceptionally(
            IllegalStateException("Server exited with non-zero status code: $exitValue")
        )
    }

    private fun printLogsInError() {
        logger.error(formatLog("Server logs:"))
        lines.forEach { logger.error(it) }
    }

    private fun formatLog(message: String): String = "(${specification.id}) $message"

    /**
     * Checks if the server is currently running.
     *
     * @return `true` if the server is running, `false` otherwise
     */
    fun isRunning() = process?.isAlive ?: false

    private companion object {
        private const val SUCCESS = 0
        private const val SIG_KILL = 137
        private const val SIG_TERM = 143

        private fun isExitValueInvalid(exitValue: Int?): Boolean =
            exitValue != SUCCESS && exitValue != SIG_KILL && exitValue != SIG_TERM

    }

}