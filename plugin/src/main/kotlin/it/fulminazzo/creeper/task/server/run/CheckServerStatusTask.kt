package it.fulminazzo.creeper.task.server.run

import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ServerConnector
import it.fulminazzo.creeper.extension.spec.ServerSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.net.Socket

/**
 * Task to check the status of the server.
 *
 * The status is checked against the status file.
 * If the status file does not exist, the server is considered to be stopped.
 * If the status file exists, the internal data is checked:
 * - If the PID is not associated with any process, the server is considered to be stopped;
 * - If it is not possible to connect to the TCP server, the server is considered to be stopped.
 *
 * If any of the above conditions are met, the status file will be deleted.
 *
 * This task should always be run to ensure proper functioning.
 *
 * @constructor Creates a new Check server status task
 */
abstract class CheckServerStatusTask : DefaultTask() {

    @get:Internal
    abstract val specification: Property<ServerSpec<*, *>>

    @get:Internal
    abstract val port: Property<Int>

    @get:Internal
    abstract val statusFile: RegularFileProperty

    @get:Internal
    abstract val stopRequiredFile: RegularFileProperty

    @TaskAction
    fun run() {
        logger.lifecycle("Checking server status")

        val statFile = statusFile.get().asFile
        if (!statFile.exists()) return writeStopRequiredFile()

        val data = CreeperPlugin.PROPERTIES_MAPPER.readValue<Map<String, Any>>(statFile)

        val pid = data["pid"]?.toString()?.toLongOrNull() ?: return writeStopRequiredFile()
        ProcessHandle.of(pid).orElse(null)?.takeIf { it.isAlive } ?: return writeStopRequiredFile()

        val port = data["port"]?.toString()?.toIntOrNull()
        if (port == null || !ServerConnector.isServerOnline(port)) return writeStopRequiredFile()
    }

    private fun writeStopRequiredFile() {
        stopRequiredFile.get().asFile.createNewFile()
    }

}