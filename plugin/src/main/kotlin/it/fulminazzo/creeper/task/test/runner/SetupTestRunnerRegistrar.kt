package it.fulminazzo.creeper.task.test.runner

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import org.gradle.api.Project
import org.gradle.api.Task
import java.nio.file.Path

/**
 * A registrar for tasks related to setting up the `tester` module project.
 *
 * @property showWorkers if `true`, all tasks will be shown by default when running `gradle tasks`
 * meaning they will show up when executing `gradle tasks`.
 * Although convenient, this can be noisy, so it should only be used for debugging.
 * @constructor Creates a new Setup test runner registrar
 */
class SetupTestRunnerRegistrar(
    private val showWorkers: Boolean = false
) {
    internal lateinit var buildDirectory: Path

    internal lateinit var project: Project

    internal lateinit var repositoryDirectory: Path

    /**
     * Registers the tasks for setting up the test runner project.
     *
     * @param project the project to register the tasks for
     * @param buildDirectory the directory where the build files are located
     */
    internal fun register(project: Project, buildDirectory: Path) {
        this.project = project
        this.buildDirectory = buildDirectory
        this.repositoryDirectory = buildDirectory.resolve(ProjectInfo.NAME)

        val fetch = registerFetchRepositoryTask()
        val write = registerWriteTestRunnerMetadataTask()
        val build = registerBuildTestRunnerTask()
        build.dependsOn(write)
        write.dependsOn(fetch)
    }

    /**
     * Registers the `buildTestRunner` task for building the test runner project.
     *
     * @return the task that was registered
     */
    internal fun registerBuildTestRunnerTask(): Task =
        CreeperPlugin.registerTask(
            project = project,
            name = "buildTestRunner",
            group = ProjectInfo.NAME.takeIf { showWorkers },
            description = "Builds the test runner project",
            type = BuildTestRunnerTask::class.java
        ) { task ->
            task.projectDirectory.set(repositoryDirectory.toFile())
        }.get()

    /**
     * Registers the `writeTestRunnerMetadata` task for writing the test runner metadata file.
     *
     * @return the task that was registered
     */
    internal fun registerWriteTestRunnerMetadataTask(): Task =
        CreeperPlugin.registerTask(
            project = project,
            name = "writeTestRunnerMetadata",
            group = ProjectInfo.NAME.takeIf { showWorkers },
            description = "Writes the test runner metadata file",
            type = WriteTestRunnerMetadataTask::class.java
        ) { task ->
            task.buildFile.set(buildDirectory.resolve("build.gradle.kts").toFile())
        }.get()

    /**
     * Registers the `fetchRepository` task for fetching the project GitHub repository.
     *
     * @return the task that was registered
     */
    internal fun registerFetchRepositoryTask(): Task =
        CreeperPlugin.registerTask(
            project = project,
            name = "fetchRepository",
            group = ProjectInfo.NAME.takeIf { showWorkers },
            description = "Fetches the project GitHub repository",
            type = FetchRepositoryTask::class.java
        ) { task ->
            task.repositoryDirectory.set(repositoryDirectory.toFile())
        }.get()

    companion object {

        /**
         * Registers a new task to set up the test runner project to inject in the running servers.
         *
         * @param project the project to register the tasks for
         * @param buildDirectory the directory where the build files are located
         */
        fun register(project: Project, buildDirectory: Path) =
            SetupTestRunnerRegistrar().register(project, buildDirectory)

    }

}