package it.fulminazzo.creeper.task.server.run

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.extension.spec.ServerSpec
import org.gradle.api.Project
import org.gradle.api.Task
import java.nio.file.Path

/**
 * A registrar for tasks related to running a server.
 *
 * @property specification the specification of the server to run
 * @property runnerExecutable the executable for running the server in a controlled environment
 * @property showWorkers if `true`, all tasks will be shown by default when running `gradle tasks`
 * meaning they will show up when executing `gradle tasks`.
 * Although convenient, this can be noisy, so it should only be used for debugging.
 * @constructor Creates a new Run server task registrar
 */
class RunServerTaskRegistrar internal constructor(
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
    internal lateinit var stopRequiredFile: Path

    internal lateinit var stopServerTask: Task

    /**
     * Registers the tasks for running the server.
     *
     * @param project the project to register the tasks for
     * @param directory the directory where the server files are located
     * @return the `checkServer[ServerSpec.id]` and `run[ServerSpec.id]` tasks
     */
    internal fun register(project: Project, directory: Path): Pair<Task, Task> {
        this.project = project
        this.serverDirectory = directory.resolve(serverId)

        this.statusFile = serverDirectory.resolve(STATUS_FILE_NAME)
        this.stopRequiredFile = serverDirectory.resolve(STOP_REQUIRED_FILE_NAME)

        stopServerTask = registerStopServerTask().get()

        val check = registerCheckServerStatusTask()
        val startServer = registerStartServerTask()
        val await = registerAwaitBootCompleteTask()
        startServer.get().dependsOn(check)
        await.get().dependsOn(startServer)

        val runServer = CreeperPlugin.registerTask<Task>(
            project = project,
            name = "run$taskBaseName",
            group = serverDisplayName,
            description = "Runs the server $serverDisplayName"
        ) { task ->
            task.dependsOn(await)
        }

        CreeperPlugin.registerTask<Task>(
            project = project,
            name = "stop$taskBaseName",
            group = serverDisplayName,
            description = "Stops the server $serverDisplayName"
        ) { task ->
            val stopFile = stopRequiredFile
            task.doLast { stopFile.toFile().createNewFile() }

            task.finalizedBy(stopServerTask)
        }

        return check.get() to runServer.get()
    }

    /**
     * Registers a `checkServer[ServerSpec.id]` task for checking the status of the server.
     *
     * @return the task that was registered
     */
    internal fun registerCheckServerStatusTask() = CreeperPlugin.registerTask(
        project = project,
        name = "checkServer$taskBaseName",
        group = serverDisplayName.takeIf { showWorkers },
        description = "Checks the status of the server $serverDisplayName",
        type = CheckServerStatusTask::class.java
    ) { task ->
        task.specification.set(specification)
        task.port.set(DEFAULT_TCP_SERVER_PORT)
        val statFile = statusFile.toFile()
        task.statusFile.set(statFile)
        task.stopRequiredFile.set(stopRequiredFile.toFile())

        task.finalizedBy(stopServerTask)

        task.onlyIf { statFile.exists() }
    }

    /**
     * Registers a `startServer[ServerSpec.id]` task for starting the server.
     *
     * @return the task that was registered
     */
    internal fun registerStartServerTask() = CreeperPlugin.registerTask(
        project = project,
        name = "startServer$taskBaseName",
        group = serverDisplayName.takeIf { showWorkers },
        description = "Starts the server $serverDisplayName",
        type = StartServerTask::class.java
    ) { task ->
        task.specification.set(specification)
        task.port.set(DEFAULT_TCP_SERVER_PORT)
        task.runnerJar.set(runnerExecutable.toFile())
        task.statusFile.set(statusFile.toFile())

        task.finalizedBy(stopServerTask)
    }

    /**
     * Registers a `awaitBootComplete[ServerSpec.id]` task for waiting for the server to complete the boot process.
     *
     * @return the task that was registered
     */
    internal fun registerAwaitBootCompleteTask() = CreeperPlugin.registerTask(
        project = project,
        name = "awaitBootComplete$taskBaseName",
        group = serverDisplayName.takeIf { showWorkers },
        description = "Waits for the server $serverDisplayName to complete the boot process",
        type = AwaitBootCompleteTask::class.java
    ) { task ->
        task.specification.set(specification)
        val statFile = statusFile.toFile()
        task.statusFile.set(statFile)
        task.logFile.set(serverDirectory.resolve("logs/latest.log").toFile())
        task.awaitTimeout.set(DEFAULT_BOOT_AWAIT_TIMEOUT)
        task.stopRequiredFile.set(stopRequiredFile.toFile())

        task.finalizedBy(stopServerTask)

        task.onlyIf { statFile.exists() }
    }

    /**
     * Registers a `stopServer[ServerSpec.id]` task for stopping the server.
     *
     * @return the task that was registered
     */
    internal fun registerStopServerTask() = CreeperPlugin.registerTask(
        project = project,
        name = "stopServer$taskBaseName",
        group = serverDisplayName.takeIf { showWorkers },
        description = "Stops the server $serverDisplayName",
        type = StopServerTask::class.java
    ) { task ->
        val stopFile = stopRequiredFile.toFile()
        task.stopRequiredFile.set(stopFile)
        task.awaitTimeout.set(DEFAULT_STOP_TIMEOUT)
        task.statusFile.set(statusFile.toFile())

        task.onlyIf { stopFile.exists() }
    }

    companion object {
        internal const val DEFAULT_TCP_SERVER_PORT = 10602
        internal const val DEFAULT_BOOT_AWAIT_TIMEOUT = 30L
        internal const val DEFAULT_STOP_TIMEOUT = 5L
        internal const val STATUS_FILE_NAME = "${ProjectInfo.NAME}-status.properties"
        internal const val STOP_REQUIRED_FILE_NAME = "stop-required"

        /**
         * Registers a new task to run the requested server.
         * The task will be named `run[ServerSpec.id]` and will be divided into smaller sub-tasks:
         * - `checkServer[ServerSpec.id]` to check the status of the server;
         * - `startServer[ServerSpec.id]` to start the server (if needed);
         * - `awaitBootComplete[ServerSpec.id]` to verify that the server has completed the boot process.
         *
         * Then, a special task will be registered:
         * - `stopServer[ServerSpec.id]` to stop the server (used by some of the above tasks in case of failure).
         *
         * @param project the project to register the task in
         * @param specification the specification of the server to install
         * @param runnerExecutable the executable for running the server in a controlled environment
         * @param directory the directory where the server files are stored
         * @return the `checkServer[ServerSpec.id]` and `run[ServerSpec.id]` tasks
         */
        fun register(
            project: Project,
            specification: ServerSpec<*, *>,
            runnerExecutable: Path,
            directory: Path
        ) = RunServerTaskRegistrar(specification, runnerExecutable).register(project, directory)

    }

}