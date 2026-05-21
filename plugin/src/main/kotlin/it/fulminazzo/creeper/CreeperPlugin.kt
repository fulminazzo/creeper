package it.fulminazzo.creeper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import it.fulminazzo.creeper.extension.ServersConfigurationExtension
import it.fulminazzo.creeper.service.PlayerResolverService
import it.fulminazzo.creeper.service.downloader.CachedDownloaderService
import it.fulminazzo.creeper.service.downloader.DownloaderService
import it.fulminazzo.creeper.service.provider.ConfigProviderService
import it.fulminazzo.creeper.service.provider.JarProviderService
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import it.fulminazzo.creeper.task.server.InstallServerRunnerTask
import it.fulminazzo.creeper.task.server.install.InjectTestRunnerRequestTask
import it.fulminazzo.creeper.task.server.install.InstallServerTaskRegistrar
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.nio.file.Path

/**
 * A simple 'hello world' plugin.
 */
class CreeperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val gradle = project.gradle

        // SERVICES
        val sharedServices = gradle.sharedServices
        val simpleDownloaderService = sharedServices
            .registerIfAbsent("simpleDownloaderService", DownloaderService::class.java)
        val downloadService = sharedServices
            .registerIfAbsent("downloaderService", CachedDownloaderService::class.java) {
                it.parameters.downloader.set(simpleDownloaderService)
            }
        sharedServices.registerIfAbsent("playerResolverService", PlayerResolverService::class.java)
        sharedServices.registerIfAbsent("jarProviderService", JarProviderService::class.java) {
            it.parameters.downloader.set(downloadService)
        }
        sharedServices.registerIfAbsent("configProviderService", ConfigProviderService::class.java) {
            it.parameters.downloader.set(downloadService)
        }
        sharedServices.registerIfAbsent("pluginProviderService", PluginProviderService::class.java) {
            it.parameters.downloader.set(downloadService)
        }

        // EXTENSIONS
        val serversConfigExtension = project.extensions.create(
            ProjectInfo.NAME.lowercase(),
            ServersConfigurationExtension::class.java
        )

        val workDir = project.layout.buildDirectory.get().asFile.resolve(ProjectInfo.NAME).toPath()
        val serversDir = workDir.resolve("servers")

        // TASKS
        serversConfigExtension.specifications.forEach { spec ->
            val serverDir = serversDir.resolve(spec.id)

            val (executable, install) = InstallServerTaskRegistrar.register(
                project,
                spec,
                serversDir
            )

            val injectTestRunnerRequestTask = registerTask(
                project,
                "inject${spec.id}TestRunner",
                null,
                "Injects the test runner into the server installation process",
                InjectTestRunnerRequestTask::class.java
            ) { task ->
                task.specification.set(spec)
                task.pluginConfigurationFile.set {
                    serverDir.resolve("plugins")
                        .resolve("${ProjectInfo.NAME}Tester")
                        .resolve("config.yml")
                        .toFile()
                }
            }
            executable.dependsOn(injectTestRunnerRequestTask)

            val installServerRunnerTask = registerTask(
                project,
                "install${spec.id}ServerRunner",
                null,
                "Installs the server runner in the server directory",
                InstallServerRunnerTask::class.java
            ) { task ->
                task.serverRunner.set(serverDir.resolve("server-runner.jar").toFile())
            }
        }
    }

    companion object {
        /**
         * The global cache directory.
         */
        internal val CACHE_DIRECTORY
            get() = Path.of(System.getProperty("user.home"), ".gradle", "caches", ProjectInfo.NAME)

        internal val JSON_MAPPER = jacksonObjectMapper()
        internal val YAML_MAPPER = YAMLMapper.builder().addModule(kotlinModule()).build()
        internal val PROPERTIES_MAPPER = JavaPropsMapper.builder().addModule(kotlinModule()).build()

        /**
         * Registers a task with the given name and description.
         *
         * @param T the type of the task
         * @param project the project to register the task in
         * @param name the name of the task
         * @param description the description of the task
         * @param type the type of the task
         * @param configuration the configuration to apply for the task
         * @return the created task provider
         */
        @Suppress("UNCHECKED_CAST")
        internal fun <T : Task> registerTask(
            project: Project,
            name: String,
            group: String? = null,
            description: String? = null,
            type: Class<T> = Task::class.java as Class<T>,
            configuration: Action<T> = {}
        ): TaskProvider<T> = project.tasks.register(name, type) {
            it.group = group
            it.description = description
            configuration.execute(it)
        }

        /**
         * Gets an appropriate Jackson mapper for the given format.
         *
         * @param format the format of the mapper (file extension)
         * @return the mapper
         */
        internal fun getMapper(format: String): ObjectMapper = when (format) {
            "json" -> JSON_MAPPER
            "yaml", "yml" -> YAML_MAPPER
            "properties" -> PROPERTIES_MAPPER
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }

    }

}
