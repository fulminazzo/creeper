package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.ServerSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

/**
 * Task to install a server executable.
 *
 * @param T the type of the server platform
 * @constructor Creates a new Install executable task
 */
abstract class InstallExecutableTask<T : ServerType> : DefaultTask() {

    @get:ServiceReference("jarProvider")
    abstract val jarProvider: JarProvider<T>

    @get:Input
    abstract val specification: Property<ServerSpec<T, *>>

    @get:Input
    abstract val serverDirectory: Property<String>

    @get:OutputFile
    abstract val executable: RegularFileProperty

    @TaskAction
    fun run() {
        val spec = specification.get()
        val directory = Path.of(serverDirectory.get())
        logger.lifecycle("Installing server ${spec.type.name} ${spec.version} in: $directory")
        val executablePath = jarProvider.get(spec.type, spec.version, directory).join()
        executable.set(executablePath.toFile())
    }

}