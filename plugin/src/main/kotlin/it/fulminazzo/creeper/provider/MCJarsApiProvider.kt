package it.fulminazzo.creeper.provider

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import it.fulminazzo.creeper.Hashable
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.server.ServerType
import it.fulminazzo.creeper.util.HttpUtils
import org.slf4j.Logger
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * A [JarProvider] and [MinecraftConfigProvider] that uses the [MCJars API](https://mcjars.app).
 *
 * @property downloader the internal downloader
 * @property logger the logger to display errors
 * @property executor the executor to use for asynchronous operations
 * @constructor Creates a new MCJars API provider
 */
class MCJarsApiProvider(
    private val downloader: CachedDownloader,
    private val logger: Logger,
    private val executor: Executor
) : JarProvider, MinecraftConfigProvider {
    private val cache = ConcurrentHashMap<Pair<ServerType, String>, CompletableFuture<BuildResponse?>>()

    /**
     * Attempts to get the requested build information from the API.
     *
     * @param type the platform
     * @param version the version of the build
     * @return the build information (or `null` if the build was not found)
     * @throws it.fulminazzo.creeper.util.HttpUtils.ApiException if the API returns an error
     */
    internal fun fetchBuild(type: ServerType, version: String): CompletableFuture<BuildResponse?> =
        cache.computeIfAbsent(type to version) {
            logger.info("Fetching build information for Minecraft ${type.name} $version")
            HttpUtils.getApi("$API_URL${getBuildUrl(type, version)}", executor).thenApply { raw ->
                raw ?: return@thenApply null
                val data = MAPPER.readValue<RawBuildResponse>(raw).builds.data.firstOrNull()
                    ?: return@thenApply null
                val installation = data.installation.firstOrNull()?.firstOrNull()
                    ?: return@thenApply null
                BuildResponse(data.uuid, installation.size, installation.url)
            }
        }

    /**
     * Attempts to get the requested configuration information from the API.
     *
     * @param name the name of the configuration
     * @param type the platform of the build containing the configuration
     * @param version the version of the build containing the configuration
     * @return the configuration information (or `null` if the configuration or build were not found)
     * @throws it.fulminazzo.creeper.util.HttpUtils.ApiException if the API returns an error
     */
    internal fun fetchConfig(
        name: String,
        type: ServerType.MinecraftType,
        version: String
    ): CompletableFuture<Config?> =
        fetchBuild(type, version).thenCompose { build ->
            logger.info("Fetching configuration '$name' for Minecraft ${type.name} $version")
            build ?: return@thenCompose CompletableFuture.completedFuture(null)
            HttpUtils.getApi("$API_URL${getBuildConfigUrl(build.uuid)}", executor).thenApply { raw ->
                raw ?: return@thenApply null
                MAPPER.readValue<ConfigResponse>(raw).configs.firstOrNull { it.name.endsWith(name) }
            }
        }

    override fun get(platform: ServerType, version: String, directory: Path): CompletableFuture<Path> =
        fetchBuild(platform, version).thenCompose { buildResponse ->
            val build = buildResponse ?: throw JarNotFoundException(platform, version)
            logger.info("Downloading Minecraft ${platform.name} $version")
            downloader.download(
                build.url,
                directory.resolve("${platform.id}-$version.jar"),
                build.toHashString()
            )
        }

    override fun get(
        name: String,
        platform: ServerType.MinecraftType,
        version: String,
        directory: Path
    ): CompletableFuture<Path> =
        fetchConfig(name, platform, version).thenApply { config ->
            val resolved = config ?: throw ConfigurationNotFoundException(name, platform, version)
            val destination = directory.createDirectories().resolve(resolved.name)
            destination.writeText(resolved.data)
            destination
        }

    companion object {
        private const val API_URL = "https://mcjars.app/api/v3/"

        private val MAPPER = jacksonObjectMapper()

        /**
         * Gets the URL to get the build information for the given [type] and [version].
         *
         * @param type the platform
         * @param version the version of the build
         * @return the URL
         */
        private fun getBuildUrl(type: ServerType, version: String): String =
            "builds/types/${type.name.uppercase()}/versions/$version"

        /**
         * Gets the URL to get the configurations for the given [buildId].
         *
         * @param buildId the UUID of the build
         * @return the URL
         */
        private fun getBuildConfigUrl(buildId: UUID): String = "builds/$buildId/configs"

    }

    /*
     * BUILD
     */

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RawBuildResponse(val builds: BuildPage)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BuildPage(val data: List<BuildData>)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class BuildData(val uuid: UUID, val installation: List<List<Installation>>)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Installation(val url: String, val size: Long)

}

/**
 * The response when querying the API for a specific build.
 *
 * @property uuid the UUID of the build
 * @property size the size of the build
 * @property url the URL to download the build
 * @constructor Create a new Build response
 */
internal data class BuildResponse(val uuid: UUID, val size: Long, val url: String) : Hashable {

    override fun toHashString(): String = "$uuid:$size"

}

/**
 * The response when querying the API for the configurations of a specific build.
 *
 * @property configs the configurations
 * @constructor Create a new Config response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ConfigResponse(val configs: List<Config>)

/**
 * Holds a configuration from the API.
 *
 * @property uuid the UUID of the configuration
 * @property data the actual configuration contents
 * @constructor Create a new Configuration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Config(
    @JsonProperty("value_uuid")
    val uuid: UUID,

    @JsonProperty("location")
    val name: String,

    @JsonProperty("value")
    val data: String

)
