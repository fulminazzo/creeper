package it.fulminazzo.creeper.provider

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import it.fulminazzo.creeper.Hashable
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.server.ServerType
import org.slf4j.Logger
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * A [MinecraftJarProvider] and [MinecraftConfigProvider] that uses the [MCJars API](https://mcjars.app).
 *
 * @property downloader the internal downloader
 * @property logger the logger to display errors
 * @constructor Creates a new MCJars API provider
 */
class MCJarsApiProvider(
    private val downloader: CachedDownloader,
    private val logger: Logger
) : MinecraftJarProvider, MinecraftConfigProvider {
    private val cache = ConcurrentHashMap<Pair<ServerType, String>, CompletableFuture<BuildResponse?>>()

    /**
     * Attempts to get the requested build information from the API.
     *
     * @param type the platform
     * @param version the version of the build
     * @return the build information (or `null` if the build was not found)
     * @throws ApiException if the API returns an error
     */
    internal fun fetchBuild(type: ServerType.MinecraftType, version: String): CompletableFuture<BuildResponse?> =
        cache.computeIfAbsent(type to version) {
            logger.info("Fetching build information for Minecraft ${type.name} $version")
            getApi(getBuildUrl(type, version)).thenApply { raw ->
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
     * @throws ApiException if the API returns an error
     */
    internal fun fetchConfig(
        name: String,
        type: ServerType.MinecraftType,
        version: String
    ): CompletableFuture<Config?> =
        fetchBuild(type, version).thenCompose { build ->
            logger.info("Fetching configuration '$name' for Minecraft ${type.name} $version")
            build ?: return@thenCompose CompletableFuture.completedFuture(null)
            getApi(getBuildConfigUrl(build.uuid)).thenApply { raw ->
                raw ?: return@thenApply null
                MAPPER.readValue<ConfigResponse>(raw).configs.firstOrNull { it.name.endsWith(name) }
            }
        }

    /**
     * Executes a GET request to the given [url] and returns the response body.
     *
     * @param url the url
     * @return the body (or `null` if the resource was not found)
     * @throws ApiException if the API returns an error
     */
    internal fun getApi(url: String): CompletableFuture<String?> = CompletableFuture.supplyAsync {
        val request = HttpRequest.newBuilder()
            .header("User-Agent", ProjectInfo.USER_AGENT)
            .uri(URI.create("$API_URL$url"))
            .build()
        val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
        when (response.statusCode()) {
            200 -> response.body()
            404 -> null
            else -> {
                val error = MAPPER.readValue<ErrorResponse>(response.body())
                throw ApiException(response.statusCode(), error)
            }
        }
    }

    override fun get(platform: ServerType.MinecraftType, version: String, directory: Path): CompletableFuture<Path> =
        fetchBuild(platform, version).thenCompose { buildResponse ->
            val build = buildResponse ?: throw JarNotFoundException(platform, version)
            logger.info("Downloading Minecraft ${platform.name} $version")
            downloader.download(
                build.url,
                directory.resolve("${platform.name.lowercase()}-$version.jar"),
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

        private val CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(30.seconds.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
        private val MAPPER = jacksonObjectMapper()

        /**
         * Gets the URL to get the build information for the given [type] and [version].
         *
         * @param type the platform
         * @param version the version of the build
         * @return the URL
         */
        private fun getBuildUrl(type: ServerType.MinecraftType, version: String): String =
            "builds/types/${type.name.uppercase()}/versions/$version"

        /**
         * Gets the URL to get the configurations for the given [buildId].
         *
         * @param buildId the UUID of the build
         * @return the URL
         */
        private fun getBuildConfigUrl(buildId: UUID): String = "builds/$buildId/configs"

    }

    /**
     * Exception thrown on unknown response code while querying the API.
     *
     * @constructor Create an empty Api exception
     *
     * @param statusCode the status code
     * @param response the response from the API
     */
    class ApiException internal constructor(statusCode: Int, response: ErrorResponse) : Exception(
        "Unexpected response code: $statusCode. Errors: ${response.errors.joinToString(", ")}"
    )

    /**
     * Identifies an error response from the API.
     *
     * @property errors the list of errors
     * @constructor Create a new Error response
     */
    internal data class ErrorResponse(val errors: List<String>)

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
