package it.fulminazzo.creeper.task.server.run

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.extension.spec.ServerSpec
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task to start a server from its specification.
 * The server will be started in the expected directory
 * (computed from the current working directory and the server specification).
 *
 * When starting the server, a new detached process will be spawned.
 * To provide easy interaction, the server will be spawned in a containerized environment,
 * which will start a TCP server at the given port.
 *
 * The status file will be used to keep track of the process:
 * it will contain the PID of the process,
 * the port the TCP server is listening to and
 * the server directory.
 *
 * @constructor Creates a new Start server task
 */
abstract class StartServerTask : DefaultTask() {

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:Input
    abstract val port: Property<Int>

    @get:Input
    abstract val runnerJar: RegularFileProperty

    @get:OutputFile
    abstract val statusFile: RegularFileProperty

    @TaskAction
    fun run() {
        val runnerJarFile = runnerJar.get().asFile
        val workDir = runnerJarFile.parentFile

        val spec = specification.get()
        val serverDir = File(workDir, spec.id)

        val tcpServerPort = port.get()

        val process = ProcessBuilder(
            "java", "-jar", runnerJarFile.name,
            tcpServerPort.toString(),
            serverDir.absolutePath,
            //TODO: change version according to specification
            "java", "jar",
            *spec.settings.flags.split(" ").toTypedArray(),
            "${spec.id}.jar",
            "nogui"
        ).directory(workDir).redirectErrorStream(true).start()

        if (process.isAlive) {
            val data = mapOf(
                "pid" to process.pid(),
                "port" to tcpServerPort,
                "serverId" to spec.id
            )
            CreeperPlugin.PROPERTIES_MAPPER.writeValue(statusFile.get().asFile, data)
        } else {
            throw GradleException(
                "Could not create server process: ${
                    process.inputStream.readAllBytes().decodeToString()
                }"
            )
        }
    }

}