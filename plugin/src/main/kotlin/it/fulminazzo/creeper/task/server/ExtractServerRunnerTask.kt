package it.fulminazzo.creeper.task.server

import it.fulminazzo.creeper.ProjectInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to extract the server runner JAR.
 *
 * @constructor Creates a new Extract server runner task
 */
abstract class ExtractServerRunnerTask : DefaultTask() {

    @get:OutputFile
    abstract val runnerJar: RegularFileProperty

    @TaskAction
    fun run() {
        val resource = checkNotNull(ExtractServerRunnerTask::class.java.getResourceAsStream(RESOURCE_PATH)) {
            "Could not find server runner JAR resource at $RESOURCE_PATH"
        }
        val file = runnerJar.get().asFile
        file.parentFile.mkdirs()
        file.writeBytes(resource.readBytes())
    }

    private companion object {
        private const val RESOURCE_PATH = "/${ProjectInfo.NAME}/server-runner.jar"

    }

}