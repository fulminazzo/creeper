package it.fulminazzo.creeper.task.server.start

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.extension.spec.ServerSpec
import org.gradle.api.Project
import org.gradle.api.Task
import java.nio.file.Path

/**
 * A registrar for tasks related to the start of a server.
 *
 * @property specification the specification of the server to run
 * @property runnerExecutable the executable for running the server in a controlled environment
 * @property showWorkers if `true`, all tasks will be shown by default when running `gradle tasks`
 * meaning they will show up when executing `gradle tasks`.
 * Although convenient, this can be noisy, so it should only be used for debugging.
 * @constructor Creates a new Start server task registrar
 */
class StartServerTaskRegistrar internal constructor(
    private val specification: ServerSpec<*, *>,
    private val runnerExecutable: Path,
    private val showWorkers: Boolean = false
) {
    private val serverId = specification.id

    private val serverDisplayName = "${specification.type.name} ${specification.version}"

    private val taskBaseName = serverId
        .replaceFirstChar { it.uppercaseChar() }
        .replace(".", "_")
        .replace("-", "")

    internal lateinit var serverDirectory: Path

    internal lateinit var project: Project

    internal lateinit var statusFile: Path
    internal lateinit var requestedStartFile: Path
    internal lateinit var requestedStopFile: Path

    /**
     * Registers the tasks for running the server.
     *
     * @param project the project to register the tasks for
     * @param directory the directory where the server files are located
     * @return the `checkServer[ServerSpec.id]` and `start[ServerSpec.id]` tasks
     */
    internal fun register(project: Project, directory: Path): Pair<Task, Task> {
        this.project = project
        this.serverDirectory = directory.resolve(serverId)

        this.statusFile = serverDirectory.resolve(STATUS_FILE_NAME)
        this.requestedStartFile = serverDirectory.resolve(REQUESTED_START_FILENAME)
        this.requestedStopFile = serverDirectory.resolve(REQUESTED_STOP_FILENAME)

        val (check, checkRequireStop) = registerCheckServerTask()
        val run = registerRunTask()
        val (await, awaitRequireStop) = registerAwaitBootCompleteTask()

        await.dependsOn(run)
        run.dependsOn(checkRequireStop)

        val startServer = CreeperPlugin.registerTask<Task>(
            project = project,
            name = "start$taskBaseName",
            group = serverDisplayName,
            description = "Starts the server $serverDisplayName"
        ) { task ->
            task.dependsOn(awaitRequireStop)
        }.get()

        registerStopTask()

        return check to startServer
    }

    /**
     * Registers a `checkServer[ServerSpec.id]` task for checking the status of the server.
     *
     * @return the task that was registered with its requested stop check task
     */
    internal fun registerCheckServerTask(): Pair<Task, Task> {
        val task = CreeperPlugin.registerTask(
            project = project,
            name = "checkServer$taskBaseName",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Checks the status of the server $serverDisplayName",
            type = CheckServerStatusTask::class.java
        ) { task ->
            task.specification.set(specification)

            task.statusFile.set(statusFile.toFile())

            task.requestedStartFile.set(requestedStartFile.toFile())
            task.requestedStopFile.set(requestedStopFile.toFile())
        }.get()
        val check = registerCheckStopRequiredTask("CheckServer")
        check.dependsOn(task)
        return task to check
    }

    /**
     * Registers a `run[ServerSpec.id]` task for running the server.
     *
     * @return the task that was registered
     */
    internal fun registerRunTask() = CreeperPlugin.registerTask(
        project = project,
        name = "run$taskBaseName",
        group = serverDisplayName.takeIf { showWorkers },
        description = "Runs the server $serverDisplayName",
        type = RunServerTask::class.java
    ) { task ->
        task.requestedStartFile.set(requestedStartFile.toFile())

        task.specification.set(specification)
        task.port.set(DEFAULT_TCP_SERVER_PORT)
        task.runnerJar.set(runnerExecutable.toFile())

        task.statusFile.set(statusFile.toFile())
    }.get()

    /**
     * Registers a `awaitBootComplete[ServerSpec.id]` task for waiting for the server to complete the boot process.
     *
     * @return the task that was registered with its requested stop check task
     */
    internal fun registerAwaitBootCompleteTask(): Pair<Task, Task> {
        val task = CreeperPlugin.registerTask(
            project = project,
            name = "awaitBootComplete$taskBaseName",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Waits for the server $serverDisplayName to complete the boot process",
            type = AwaitBootCompleteTask::class.java
        ) { task ->
            task.logFile.set(serverDirectory.resolve("logs/latest.log").toFile())
            task.awaitTimeout.set(DEFAULT_BOOT_AWAIT_TIMEOUT)
            task.statusFile.set(statusFile.toFile())

            task.specification.set(specification)

            task.requestedStopFile.set(requestedStopFile.toFile())
        }.get()
        val check = registerCheckStopRequiredTask("AwaitBootComplete")
        check.dependsOn(task)
        return task to check
    }

    /**
     * Registers a `stop[ServerSpec.id]` task for requesting a server stop.
     *
     * @return the task that was registered
     */
    internal fun registerStopTask() = CreeperPlugin.registerTask(
        project = project,
        name = "stop$taskBaseName",
        group = serverDisplayName,
        description = "Stops the server $serverDisplayName",
        type = StopServerTask::class.java
    ) { task ->
        task.awaitTimeout.set(DEFAULT_STOP_TIMEOUT)
        task.statusFile.set(statusFile.toFile())

        task.requestedStopFile.set(requestedStopFile.toFile())

        task.doFirst { requestedStopFile.toFile().createNewFile() }
    }.get()

    private fun registerCheckStopRequiredTask(requester: String) = CreeperPlugin.registerTask(
        project = project,
        name = "checkStopRequired${taskBaseName}For$requester",
        group = serverDisplayName.takeIf { showWorkers },
        description = "Checks if the server $serverDisplayName has been requested to stop",
        type = StopServerTask::class.java
    ) { task ->
        task.awaitTimeout.set(DEFAULT_STOP_TIMEOUT)
        task.statusFile.set(statusFile.toFile())

        task.requestedStopFile.set(requestedStopFile.toFile())
    }.get()

    companion object {
        internal const val DEFAULT_TCP_SERVER_PORT = 10602
        internal const val DEFAULT_BOOT_AWAIT_TIMEOUT = 30L
        internal const val DEFAULT_STOP_TIMEOUT = 5L

        internal const val STATUS_FILE_NAME = "${ProjectInfo.NAME}-status.properties"
        internal const val REQUESTED_START_FILENAME = "request-start"
        internal const val REQUESTED_STOP_FILENAME = "request-stop"

        /**
         * Registers a new task to run the requested server.
         * The task will be named `start[ServerSpec.id]` and will be divided into smaller sub-tasks:
         * - `checkServer[ServerSpec.id]` to check the status of the server;
         * - `run[ServerSpec.id]` to start the server (if needed);
         * - `awaitBootComplete[ServerSpec.id]` to verify that the server has completed the boot process.
         *
         * Then, a special task will be registered:
         * - `stop[ServerSpec.id]` to stop the server (used by some of the above tasks in case of failure).
         *
         * @param project the project to register the task in
         * @param specification the specification of the server to install
         * @param runnerExecutable the executable for running the server in a controlled environment
         * @param directory the directory where the server files are stored
         * @return the `checkServer[ServerSpec.id]` and `start[ServerSpec.id]` tasks
         */
        fun register(
            project: Project,
            specification: ServerSpec<*, *>,
            runnerExecutable: Path,
            directory: Path
        ) = StartServerTaskRegistrar(specification, runnerExecutable).register(project, directory)

    }

}