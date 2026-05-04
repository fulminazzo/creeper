package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.nio.file.Path

/**
 * A registrar for tasks related to installing a server.
 *
 * @property specification the specification of the server to install
 * @property showWorkers if `true`, all tasks will be shown by default when running `gradle tasks`
 * meaning they will show up when executing `gradle tasks`.
 * Although convenient, this can be noisy, so it should only be used for debugging.
 * @constructor Creates a new Install server task registrar
 */
class InstallServerTaskRegistrar private constructor(
    private val specification: ServerSpec<*, *>,
    private val showWorkers: Boolean = false
) {
    private val serverId = specification.id

    private val serverDisplayName = "${specification.type.name} ${specification.version}"

    private val taskBaseName = serverId
        .replaceFirstChar { it.uppercaseChar() }
        .replace(".", "_")
        .replace("-", "")
    private val installTaskName = "install${taskBaseName}"
    private val writeTaskName = "write${taskBaseName}"

    internal lateinit var serverDirectory: Path

    internal lateinit var project: Project
    internal lateinit var baseTask: Task

    internal lateinit var executableTask: Task

    private val pluginsDirectory: Path
        get() = serverDirectory.resolve("plugins")
    private val pluginsMetadataDirectory: Path
        get() = pluginsDirectory.resolve(ProjectInfo.NAME).resolve("plugins")

    internal lateinit var pluginsTask: Task

    /**
     * Registers the tasks for installing the server.
     *
     * @param project the project to register the tasks for
     * @param directory the directory where the server will be installed (the server directory will reside in here)
     */
    internal fun register(project: Project, directory: Path) {
        this.project = project
        this.serverDirectory = directory
        this.baseTask = CreeperPlugin.registerTask<Task>(
            project = project,
            name = installTaskName,
            group = serverDisplayName,
            description = "Installs the server: $serverDisplayName",
        ).get()

        registerInstallExecutableTask()

        val pluginsRequests = specification.plugins
        if (pluginsRequests.isNotEmpty()) {
            registerInstallPluginsTask()
            pluginsRequests.forEachIndexed { index, pluginRequest ->
                val number = index + 1
                val fetchTask = registerFetchPluginMetadataTask(pluginRequest, number)
                val pluginTask = registerInstallPluginTask(pluginRequest, number)
                fetchTask.dependsOn(executableTask)
                pluginTask.dependsOn(fetchTask)
                pluginsTask.dependsOn(pluginTask)
            }
        }

        if (specification is MinecraftServerSpec) {
            registerInstallProperties()
            registerWriteEula()
            registerWriteWhitelist()
            registerWriteOperators()
            if (specification.type.isForkOf(ServerType.BUKKIT)) installBukkitYml()
        }
    }

    /**
     * Registers a `install[ServerSpec.id]Executable` task for installing the server executable.
     *
     * @return the task that was registered
     */
    internal fun registerInstallExecutableTask(): Task {
        val task = CreeperPlugin.registerTask(
            project = project,
            name = "${installTaskName}Executable",
            group = serverDisplayName,
            description = "Installs the executable file of server $serverDisplayName",
            type = InstallExecutableTask::class.java
        ) { task ->
            task.specification.set(specification)
            task.executable.set(serverDirectory.resolve("${serverId}.jar").toFile())
        }
        baseTask.dependsOn(task)
        executableTask = task.get()
        return executableTask
    }

    /**
     * PLUGINS
     */

    /**
     * Registers a `install[ServerSpec.id]Plugins` task for installing the requested plugins.
     *
     * @return the task that was registered
     */
    internal fun registerInstallPluginsTask(): Task {
        pluginsTask = setDependencyHierarchy(
            CreeperPlugin.registerTask(
                project = project,
                name = "${installTaskName}Plugins",
                group = serverDisplayName,
                description = "Installs the plugins of server $serverDisplayName (if requested)",
            )
        )
        return pluginsTask
    }

    /**
     * Registers a `fetch[ServerSpec.id]Plugin[index]Metadata` task for fetching the metadata of a plugin.
     *
     * @param pluginRequest the requested plugin to fetch metadata for
     * @param index the index of the plugin in the plugin requests list
     * @return the task that was registered
     */
    internal fun registerFetchPluginMetadataTask(pluginRequest: PluginRequest, index: Int): Task =
        CreeperPlugin.registerTask(
            project = project,
            name = "fetch${taskBaseName}Plugin${index}Metadata",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Fetches the metadata for plugin #$index of server $serverDisplayName",
            type = FetchPluginMetadataTask::class.java
        ) { task ->
            task.request.set(pluginRequest)
            task.pluginMetadata.set(pluginsMetadataDirectory.toFile())
        }.get()

    /**
     * Registers a `install[ServerSpec.id]Plugin[index]` task for installing a plugin.
     *
     * @param pluginRequest the requested plugin to install
     * @param index the index of the plugin in the plugin requests list
     * @return the task that was registered
     */
    internal fun registerInstallPluginTask(pluginRequest: PluginRequest, index: Int): Task = CreeperPlugin.registerTask(
        project = project,
        name = "${installTaskName}Plugin${index}",
        group = serverDisplayName.takeIf { showWorkers },
        description = "Installs plugin #$index of server $serverDisplayName",
        type = InstallPluginTask::class.java
    ) { task ->
        task.request.set(pluginRequest)
        task.pluginMetadata.set(pluginsMetadataDirectory.toFile())
        task.pluginsDirectory.set(pluginsDirectory.toFile())
    }.get()

    /**
     * [it.fulminazzo.creeper.ServerType.MinecraftType]
     */

    /**
     * Registers a `install[ServerSpec.id]Properties` task for installing the `server.properties` file.
     * Only available on Minecraft servers.
     *
     * @return the task that was registered
     */
    internal fun registerInstallProperties() = setDependencyHierarchy(
        CreeperPlugin.registerTask(
            project = project,
            name = "${installTaskName}Properties",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Installs the server.properties file of server $serverDisplayName",
            type = InstallConfigTask::class.java
        ) { task ->
            task.action.set(ConfigAction.ServerProperties)
            task.specification.set(specification)
            task.configFile.set(serverDirectory.resolve("server.properties").toFile())
        }
    )

    /**
     * Registers a `write[ServerSpec.id]Eula` task for writing the `eula.txt` file.
     * Only available on Minecraft servers.
     */
    internal fun registerWriteEula() = setDependencyHierarchy(
        CreeperPlugin.registerTask(
            project = project,
            name = "${writeTaskName}Eula",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Installs the eula.txt file of server $serverDisplayName",
            type = WriteFileTask::class.java
        ) { task ->
            task.action.set(FileAction.Eula)
            task.specification.set(specification)
            task.file.set(serverDirectory.resolve("eula.txt").toFile())
        }
    )

    /**
     * Registers a `write[ServerSpec.id]Whitelist` task for installing the `whitelist.json` file.
     * Only available on Minecraft servers.
     *
     * @return the task that was registered
     */
    internal fun registerWriteWhitelist() = setDependencyHierarchy(
        CreeperPlugin.registerTask(
            project = project,
            name = "${writeTaskName}Whitelist",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Installs the whitelist.json file of server $serverDisplayName",
            type = WriteFileTask::class.java
        ) { task ->
            task.action.set(FileAction.Whitelist)
            task.specification.set(specification)
            task.file.set(serverDirectory.resolve("whitelist.json").toFile())
        }
    )

    /**
     * Registers a `write[ServerSpec.id]Operators` task for installing the `ops.json` file.
     * Only available on Minecraft servers.
     *
     * @return the task that was registered
     */
    internal fun registerWriteOperators() = setDependencyHierarchy(
        CreeperPlugin.registerTask(
            project = project,
            name = "${writeTaskName}Operators",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Installs the ops.json file of server $serverDisplayName",
            type = WriteFileTask::class.java
        ) { task ->
            task.action.set(FileAction.Operators)
            task.specification.set(specification)
            task.file.set(serverDirectory.resolve("ops.json").toFile())
        }
    )

    /**
     * [it.fulminazzo.creeper.ServerType.BUKKIT]
     */

    /**
     * Registers a `install[ServerSpec.id]BukkitYml` task for installing the `bukkit.yml` file.
     * Only available on Minecraft servers.
     *
     * @return the task that was registered
     */
    internal fun installBukkitYml() = setDependencyHierarchy(
        CreeperPlugin.registerTask(
            project = project,
            name = "${installTaskName}BukkitYml",
            group = serverDisplayName.takeIf { showWorkers },
            description = "Installs the bukkit.yml file of server $serverDisplayName",
            type = InstallConfigTask::class.java
        ) { task ->
            task.action.set(ConfigAction.BukkitConfig)
            task.specification.set(specification)
            task.configFile.set(serverDirectory.resolve("bukkit.yml").toFile())
        }
    )

    private fun <T : Task> setDependencyHierarchy(task: TaskProvider<T>): T {
        val t = task.get()
        baseTask.dependsOn(t.dependsOn(executableTask))
        return t
    }

    companion object {

        /**
         * Registers a new task to install the requested server.
         * The task will be named `install[ServerSpec.id]` and will be divided into smaller sub-tasks:
         * - `install[ServerSpec.id]Executable` to install the actual server executable;
         * - `fetch[ServerSpec.id]Plugin1Metadata`, `fetch[ServerSpec.id]Plugin2Metadata`, ... and
         * `install[ServerSpec.id]Plugin1`, `install[ServerSpec.id]Plugin2`, ...
         * to install the requested plugins (if any).
         *
         * Then, some platform-specific tasks will be added.
         * - If the [ServerType] is a [ServerType.MinecraftType], then the following tasks will be added:
         *   - `install[ServerSpec.id]ServerProperties` to install the server.properties file;
         *   - `install[ServerSpec.id]Whitelist` to install the whitelist.json file;
         *   - `install[ServerSpec.id]Operators` to install the ops.json file;
         *   - `install[ServerSpec.id]Eula` to install the eula.txt file;
         *   - `install[ServerSpec.id]BukkitYml` to install the bukkit.yml file
         *   (only if the [ServerType] is a fork of [ServerType.BUKKIT]).
         *
         * Each task will share the group `[ServerType.name] [ServerSpec.version]`.
         *
         * @param project the project to register the task in
         * @param specification the specification of the server to install
         * @param directory the directory where the server files are stored
         */
        fun register(project: Project, specification: ServerSpec<*, *>, directory: Path) =
            InstallServerTaskRegistrar(specification).register(project, directory)

    }

}