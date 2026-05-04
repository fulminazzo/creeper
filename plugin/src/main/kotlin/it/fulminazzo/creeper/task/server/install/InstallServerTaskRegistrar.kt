package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.server.spec.MinecraftServerSpec
import it.fulminazzo.creeper.server.spec.ServerSpec
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.nio.file.Path

/**
 * A registrar for the installation tasks of a server.
 */
object InstallServerTaskRegistrar {

    /**
     * Registers a new task to install the requested server.
     * The task will be named `install[ServerSpec.id]` and will be divided into smaller sub-tasks:
     * - `install[ServerSpec.id]Executable` to install the actual server executable;
     * - `install[ServerSpec.id]Plugin1`, `install[ServerSpec.id]Plugin2`, ... to install the requested plugins (if any);
     * Then, some platform-specific tasks will be added.
     * - If the [ServerType] is a [ServerType.MinecraftType], then the following tasks will be added:
     *   - `install[ServerSpec.id]ServerProperties` to install the server.properties file;
     *   - `install[ServerSpec.id]BukkitYml` to install the bukkit.yml file (only if the [ServerType] is a fork of [ServerType.BUKKIT]);
     *   - `install[ServerSpec.id]Whitelist` to install the whitelist.json file;
     *   - `install[ServerSpec.id]Operators` to install the ops.json file;
     *   - `install[ServerSpec.id]Eula` to install the eula.txt file.
     *
     * @param project the project to register the task in
     * @param specification the specification of the server to install
     * @param directory the directory where the server files are stored
     * @return the registered installation task
     */
    fun register(project: Project, specification: ServerSpec<*, *>, directory: Path): TaskProvider<Task> {
        val serverId = specification.id.replaceFirstChar { it.uppercaseChar() }
        val installTaskBaseName = "install${serverId}"
        val tasks = mutableListOf<TaskProvider<*>>()
        // MANDATORY, the executable
        val serverDirectory = directory.resolve(serverId)
        tasks + project.tasks.register(
            "${installTaskBaseName}Executable",
            InstallExecutableTask::class.java
        ) { task ->
            task.specification.set(specification)
            task.executable.set(serverDirectory.resolve("${serverId}.jar").toFile())
        }
        // MANDATORY, the plugins
        val pluginsDirectory = serverDirectory.resolve("plugins")
        tasks + specification.plugins.mapIndexed { index, plugin ->
            project.tasks.register(
                "${installTaskBaseName}Plugin${index + 1}",
                InstallPluginTask::class.java
            ) { task ->
                task.request.set(plugin)
                //TODO: better naming, taken from the request
                //TODO: however how do we pull it from the Http before-hand?
                task.plugin.set(pluginsDirectory.resolve("plugin${index + 1}.jar").toFile())
            }
        }
        // PER-SPECIFICATION
        if (specification is MinecraftServerSpec) {
            // server.properties
            tasks + project.tasks.register(
                "${installTaskBaseName}ServerProperties",
                InstallConfigTask::class.java
            ) { task ->
                task.action.set(ConfigAction.ServerProperties)
                task.specification.set(specification)
                task.configFile.set(serverDirectory.resolve("server.properties").toFile())
            }
            // bukkit.yml
            if (specification.type.isForkOf(ServerType.BUKKIT))
                tasks + project.tasks.register(
                    "${installTaskBaseName}BukkitYml",
                    InstallConfigTask::class.java
                ) { task ->
                    task.action.set(ConfigAction.BukkitConfig)
                    task.specification.set(specification)
                    task.configFile.set(serverDirectory.resolve("bukkit.yml").toFile())
                }
            // whitelist.json
            tasks + project.tasks.register(
                "${installTaskBaseName}Whitelist",
                WriteFileTask::class.java
            ) { task ->
                task.action.set(FileAction.Whitelist)
                task.specification.set(specification)
                task.file.set(serverDirectory.resolve("whitelist.json").toFile())
            }
            // ops.json
            tasks + project.tasks.register(
                "${installTaskBaseName}Operators",
                WriteFileTask::class.java
            ) { task ->
                task.action.set(FileAction.Operators)
                task.specification.set(specification)
                task.file.set(serverDirectory.resolve("ops.json").toFile())
            }
            // eula.txt
            tasks + project.tasks.register(
                "${installTaskBaseName}Eula",
                WriteFileTask::class.java
            ) { task ->
                task.action.set(FileAction.Eula)
                task.specification.set(specification)
                task.file.set(serverDirectory.resolve("eula.txt").toFile())
            }
        }
        // Finally the task, dependent on everything
        return project.tasks.register(installTaskBaseName) { task -> tasks.forEach { task.dependsOn(it) } }
    }

}