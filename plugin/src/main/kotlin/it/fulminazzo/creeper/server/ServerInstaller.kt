package it.fulminazzo.creeper.server

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import it.fulminazzo.creeper.provider.ConfigProvider
import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.provider.plugin.RedirectPluginProvider
import it.fulminazzo.creeper.server.spec.ServerSpec
import it.fulminazzo.creeper.server.spec.settings.ServerSettings
import org.slf4j.Logger
import tools.jackson.databind.ObjectMapper
import tools.jackson.dataformat.javaprop.JavaPropsMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.extension

/**
 * Generic installer for a server.
 *
 * @param T the type of the server platform
 * @param C the type of the server settings
 * @param S the type of the server specification
 * @property specification the specification of the server to install
 * @property logger the logger to use for logging
 * @property jarProvider the provider of the server jar
 * @property configProvider the provider of the server configurations
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new Server installer
 */
sealed class ServerInstaller<T : ServerType, C : ServerSettings, S : ServerSpec<T, C>>(
    protected val specification: S,
    protected val logger: Logger,
    private val jarProvider: JarProvider<T>,
    private val configProvider: ConfigProvider<T>,
    private val downloader: CachedDownloader
) {

    /**
     * Installs the server in the given directory.
     *
     * @param directory the working directory where the server will be installed
     * @return the path of the installed server jar
     */
    open fun install(directory: Path): CompletableFuture<Path> = installJar(directory)
        .thenCompose { executable -> installPlugins(directory).thenApply { executable } }

    /**
     * Installs a server configuration in the given directory.
     * Then, it edits it with the given function.
     *
     * @param name the name of the configuration
     * @param directory the working directory where the server will be installed
     * @param configuration the function to apply to the configuration
     * @receiver the current configuration
     * @return the path of the installed server configuration
     */
    protected fun installAndEditConfig(
        name: String,
        directory: Path,
        configuration: MutableMap<String, Any>.() -> Unit
    ): CompletableFuture<Path> =
        installConfig(name, directory).thenApply { path ->
            val mapper = getMapper(path.extension)
            val currentConfig = mapper.readValue<MutableMap<String, Any>>(path.toFile())
            configuration(currentConfig)
            mapper.writeValue(path, currentConfig)
            path
        }

    /**
     * Installs the server jar in the given directory.
     *
     * @param directory the working directory where the server will be installed
     * @return the path of the installed server jar
     */
    private fun installJar(directory: Path): CompletableFuture<Path> {
        val serverDirectory = getServerDirectory(directory)
        logger.info("Installing server ${specification.type.name} ${specification.version} in: $serverDirectory")
        return jarProvider.get(specification.type, specification.version, serverDirectory)
    }

    /**
     * Installs a server configuration in the given directory.
     *
     * @param name the name of the configuration
     * @param directory the working directory where the server will be installed
     * @return the path of the installed server configuration
     */
    private fun installConfig(name: String, directory: Path): CompletableFuture<Path> {
        val serverDirectory = getServerDirectory(directory)
        logger.info("Installing configuration file $name for ${specification.type.name} ${specification.version} in: $serverDirectory")
        return configProvider.get(name, specification.type, specification.version, serverDirectory)
    }

    /**
     * Installs all the requested plugins in the given directory.
     *
     * @param directory the working directory where the server will be installed
     * @return the list of paths of the installed plugins
     */
    private fun installPlugins(directory: Path): CompletableFuture<List<Path>> {
        val serverDirectory = getServerDirectory(directory).resolve("plugins")
        logger.info("Installing plugins for ${specification.type.name} ${specification.version} in: $serverDirectory")
        val pluginProvider = RedirectPluginProvider(serverDirectory, logger, downloader)
        val futures = specification.plugins.map { pluginProvider.handleRequest(it) }
        return CompletableFuture.allOf(*futures.toTypedArray()).thenApply { futures.map { it.join() } }
    }

    private fun getServerDirectory(parent: Path) =
        parent.resolve("${specification.type.name.lowercase()}-${specification.version}")

    private companion object {
        private val JSON_MAPPER = jacksonObjectMapper()
        private val YAML_MAPPER = YAMLMapper.builder().addModule(kotlinModule()).build()
        private val PROPERTIES_MAPPER = JavaPropsMapper.builder().addModule(kotlinModule()).build()

        /**
         * Gets an appropriate Jackson mapper for the given format.
         *
         * @param format the format of the mapper (file extension)
         * @return the mapper
         */
        fun getMapper(format: String): ObjectMapper = when (format) {
            "json" -> JSON_MAPPER
            "yaml", "yml" -> YAML_MAPPER
            "properties" -> PROPERTIES_MAPPER
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }

    }

}