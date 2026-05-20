package it.fulminazzo.creeper.task.test.runner

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector

/**
 * A task to build the test runner.
 *
 * @constructor Creates a new Build test runner task
 */
abstract class BuildTestRunnerTask : DefaultTask() {

    @get:InputDirectory
    abstract val projectDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        val projectDir = projectDirectory.get().asFile
        logger.lifecycle("Building Tests runner in $projectDir")

        GradleConnector.newConnector()
            .forProjectDirectory(projectDir)
            .connect()
            .use { connection ->
                connection.newBuild()
                    .forTasks("build")
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
            }
    }

}