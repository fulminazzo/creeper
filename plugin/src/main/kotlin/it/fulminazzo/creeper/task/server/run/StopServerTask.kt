package it.fulminazzo.creeper.task.server.run

import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ServerConnector
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.net.Socket
import kotlin.time.Duration.Companion.seconds

/**
 * Task to stop a server.
 *
 * Uses the status file to determine if the server is running.
 * It first attempts to stop the server using the `stopprocess` command (connecting via TCP),
 * and if that fails, it kills the process forcibly.
 *
 * @constructor Creates a new Stop server task
 */
abstract class StopServerTask : DefaultTask() {

    @get:Internal
    abstract val stopRequiredFile: RegularFileProperty

    @get:Internal
    abstract val awaitTimeout: Property<Long>

    @get:Internal
    abstract val statusFile: RegularFileProperty

    @TaskAction
    fun run() {
        logger.lifecycle("Stopping server")
        val statFile = statusFile.get().asFile
        if (statFile.exists()) {
            val data = CreeperPlugin.PROPERTIES_MAPPER.readValue<Map<String, Any>>(statFile)

            val port = data["port"]?.toString()?.toIntOrNull()
            if (port != null) {
                logger.lifecycle("Attempting to stop server via TCP connection on port $port")
                try {
                    val connector = ServerConnector(port)
                    connector.connect()
                    if (connector.connected) {
                        connector.send(STOP_COMMAND)
                        connector.awaitInput(
                            ".*Internal process terminated.*".toRegex(),
                            awaitTimeout.get().seconds
                        )
                        if (connector.connected) connector.disconnect()
                    }
                } catch (_: Exception) {
                    // ignore errors
                }
            }

            logger.lifecycle("Killing server process")
            val pid = data["pid"]?.toString()?.toLongOrNull()
            if (pid != null) ProcessHandle.of(pid).ifPresent { it.destroyForcibly() }

            statFile.delete()
        }
        logger.lifecycle("Server stopped")

        stopRequiredFile.get().asFile.delete()
    }

    private companion object {
        private const val STOP_COMMAND = "stopprocess"

    }

}