package it.fulminazzo.creeper.task.server.start

import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.util.VerifyUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import kotlin.time.Duration.Companion.seconds

/**
 * Task to verify if the server has booted successfully.
 *
 * @constructor Creates a new Await boot complete task
 */
abstract class AwaitBootCompleteTask : DefaultTask() {

    @get:Internal
    abstract val logFile: RegularFileProperty

    @get:Internal
    abstract val awaitTimeout: Property<Long>

    @get:Internal
    abstract val statusFile: RegularFileProperty

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:OutputFile
    abstract val requestedStopFile: RegularFileProperty

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun run() {
        if (!statusFile.get().asFile.exists()) return

        logger.lifecycle("Awaiting server boot completion. Timeout: ${awaitTimeout.get()} seconds")

        val spec = specification.get()
        val log = logFile.get().asFile
        if (VerifyUtils.awaitVerified(
                { log.exists() && log.readLines().any { spec.isBootCompleteLine(it) } },
                awaitTimeout.get().seconds,
            )
        ) logger.lifecycle("Server booted successfully")
        else {
            requestStop()
            throw GradleException(
                "Server could not boot within ${awaitTimeout.get()} seconds. "
                        + "This could either be a problem with the process or the server might require a bigger timeout. "
                        + "Check the server log for more information: ${log.absolutePath}"
            )
        }
    }

    private fun requestStop() {
        requestedStopFile.get().asFile.createNewFile()
    }

}