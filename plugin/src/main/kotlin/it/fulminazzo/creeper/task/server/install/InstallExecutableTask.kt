package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.server.spec.ServerSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to install a server executable.
 *
 * @constructor Creates a new Install executable task
 */
abstract class InstallExecutableTask : DefaultTask() {

    @get:ServiceReference("jarProvider")
    abstract val jarProvider: JarProvider

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:OutputFile
    abstract val executable: RegularFileProperty

    @TaskAction
    fun run() {
        val spec = specification.get()
        val directory = executable.get().asFile.parentFile.toPath()
        logger.lifecycle("Installing server ${spec.type.name} ${spec.version} in: $directory")
        jarProvider.get(spec.type, spec.version, directory)
    }

}