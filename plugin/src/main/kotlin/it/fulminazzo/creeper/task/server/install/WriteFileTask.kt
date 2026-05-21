package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.service.PlayerResolverService
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.name

/**
 * Task to generate and modify a configuration file for the server.
 * This process is necessary to provide support for whitelist or other settings from the specification.
 *
 * @constructor Creates a new Write file task
 * @see FileAction
 */
abstract class WriteFileTask : DefaultTask() {

    @get:ServiceReference("playerResolverService")
    abstract val playerResolverService: Property<PlayerResolverService>

    @get:Internal
    abstract val action: Property<FileAction>

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:OutputFile
    abstract val file: RegularFileProperty

    @TaskAction
    fun run() {
        val actualFile = file.get().asFile.toPath()
        logger.lifecycle("Generating server file: ${actualFile.name}")
        action.get().apply(actualFile.parent, specification.get(), playerResolverService.get().playerResolver)
    }

}