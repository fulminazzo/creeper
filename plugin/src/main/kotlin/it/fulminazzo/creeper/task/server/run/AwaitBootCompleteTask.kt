package it.fulminazzo.creeper.task.server.run

import it.fulminazzo.creeper.extension.spec.ServerSpec
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Task to verify if the server has booted successfully.
 *
 * @constructor Creates a new Await boot complete task
 */
abstract class AwaitBootCompleteTask : DefaultTask() {

    @get:Input
    abstract val specification: Property<ServerSpec<*, *>>

    @get:InputFile
    abstract val statusFile: RegularFileProperty

    @get:InputFile
    abstract val logFile: RegularFileProperty

    @get:Input
    abstract val timeout: Property<Long>

    @TaskAction
    fun run() {
        logger.lifecycle("Awaiting server boot completion. Timeout: ${timeout.get()} seconds")
        val verified = AtomicBoolean(false)
        val latch = CountDownLatch(1)

        val scheduler = Executors.newSingleThreadScheduledExecutor()

        val spec = specification.get()
        val log = logFile.get().asFile
        scheduler.scheduleAtFixedRate(
            {
                if (!verified.get())
                    verified.set(log.exists() && log.readLines().any { spec.isBootCompleteLine(it) })
            }, 0, 1, TimeUnit.SECONDS
        )

        latch.await(timeout.get(), TimeUnit.SECONDS)
        scheduler.shutdownNow()

        if (!verified.get())
            //TODO: handle stop
            throw GradleException(
                "Server could not boot within ${timeout.get()} seconds. "
                        + "This could either be a problem with the process or the server might require a bigger timeout. "
                        + "Check the server log for more information: ${log.absolutePath}"
            )
        else logger.lifecycle("Server booted successfully")
    }

}