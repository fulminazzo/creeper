package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.service.provider.JarProviderService
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to fetch the necessary executable JAR for running a server.
 *
 * @constructor Creates a new Install executable task
 * @see JarProvider
 */
abstract class InstallExecutableTask : DefaultTask() {

    @get:ServiceReference("jarProviderService")
    abstract val jarProviderService: Property<JarProviderService>

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:OutputFile
    abstract val executable: RegularFileProperty

    @TaskAction
    fun run() {
        val spec = specification.get()
        val directory = executable.get().asFile.parentFile.toPath()
        logger.lifecycle("Installing server ${spec.type.name} ${spec.version} in: $directory")
        jarProviderService.get().provider.get(spec.type, spec.version, directory)
    }

}