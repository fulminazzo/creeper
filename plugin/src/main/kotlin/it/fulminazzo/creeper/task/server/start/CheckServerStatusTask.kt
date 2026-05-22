package it.fulminazzo.creeper.task.server.start

import com.fasterxml.jackson.module.kotlin.readValue
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ServerConnector
import it.fulminazzo.creeper.extension.spec.ServerSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

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
    abstract val statusFile: RegularFileProperty

    @get:OutputFile
    abstract val requestedStartFile: RegularFileProperty

    @get:OutputFile
    abstract val requestedStopFile: RegularFileProperty

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun run() {
        logger.lifecycle("Checking server status")

        val statFile = statusFile.get().asFile
        if (!statFile.exists()) return requestStart()

        val data = CreeperPlugin.PROPERTIES_MAPPER.readValue<Map<String, Any>>(statFile)

        val pid = data["pid"]?.toString()?.toLongOrNull() ?: return requestStop()
        ProcessHandle.of(pid).orElse(null)?.takeIf { it.isAlive } ?: return requestStop()

        val port = data["port"]?.toString()?.toIntOrNull()
        if (port == null || !ServerConnector.isServerOnline(port)) return requestStop()
    }

    private fun requestStart() {
        logger.lifecycle("Server is not running, requesting start")
        requestedStartFile.get().asFile.createNewFile()
    }

    private fun requestStop() {
        logger.lifecycle("Server is running, requesting stop")
        requestedStopFile.get().asFile.createNewFile()
        requestStart()
    }

}