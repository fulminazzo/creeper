package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.Hashable
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.uuid.Uuid

/**
 * A [MinecraftJarProvider] and [MinecraftConfigProvider] that uses the [MCJars API](https://mcjars.app).
 */
class MCJarsApiProvider : MinecraftJarProvider, MinecraftConfigProvider {
    private val downloader = CachedDownloader.global(Downloader.http())
    private val cache = mutableMapOf<Pair<ServerType, String>, BuildResponse>()

    /**
     * Gets the requested build information.
     *
     * @param type the platform
     * @param version the version of the build
     * @return the build information (or `null` if the build was not found)
     * @throws ApiException if the API returns an error
     */
    internal fun getBuild(type: ServerType.MinecraftType, version: String): BuildResponse? {
        val key = type to version
        if (key in cache) return cache[key]
        val url = getBuildUrl(type, version)
        val raw = getFromApi(url) ?: return null
        val data = Json.decodeFromString<RawBuildResponse>(raw).builds.data.firstOrNull() ?: return null
        val installation = data.installation.firstOrNull()?.firstOrNull() ?: return null
        val response = BuildResponse(data.uuid, installation.size, installation.url)
        cache[key] = response
        return response
    }

    /**
     * Gets the requested configuration information.
     *
     * @param name the name of the configuration
     * @param type the platform of the build containing the configuration
     * @param version the version of the build containing the configuration
     * @return the configuration information (or `null` if the configuration or build were not found)
     * @throws ApiException if the API returns an error
     */
    internal fun getConfig(name: String, type: ServerType.MinecraftType, version: String): Config? {
        val build = getBuild(type, version) ?: return null
        val url = getBuildConfigUrl(build.uuid)
        val raw = getFromApi(url) ?: return null
        val response = Json.decodeFromString<ConfigResponse>(raw)
        return response.configs.firstOrNull { it.name.endsWith(name) }
    }

    /**
     * Executes a GET request to the given [url] and returns the response body.
     *
     * @param url the url
     * @return the body (or `null` if the resource was not found)
     * @throws ApiException if the API returns an error
     */
    internal fun getFromApi(url: String): String? {
        val request = HttpRequest.newBuilder()
            .header("User-Agent", ProjectInfo.USER_AGENT)
            .uri(URI.create("$API_URL$url"))
            .build()
        val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
        return when (response.statusCode()) {
            200 -> response.body()
            404 -> null
            else -> {
                val error = Json.decodeFromString<ErrorResponse>(response.body())
                throw ApiException(response.statusCode(), error)
            }
        }
    }

    override fun get(platform: ServerType.MinecraftType, version: String, directory: Path) {
        getBuild(platform, version)
            ?.let {
                downloader.download(
                    it.url,
                    directory.resolve("${platform.name.lowercase()}-$version.jar"),
                    it.toHashString()
                )
            }
            ?: throw JarNotFoundException(platform, version)
    }

    override fun get(
        name: String,
        platform: ServerType.MinecraftType,
        version: String,
        directory: Path
    ) {
        getConfig(name, platform, version)
            ?.let {
                directory.createDirectories().resolve(it.name).writeText(it.data)
            }
            ?: throw ConfigurationNotFoundException(
                name,
                platform,
                version
            )
    }

    companion object {
        private const val API_URL = "https://mcjars.app/api/v3/"

        private val CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(30.seconds.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

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
        private fun getBuildConfigUrl(buildId: Uuid): String = "builds/$buildId/configs"

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
    @Serializable
    internal data class ErrorResponse(val errors: List<String>)

    /*
     * BUILD
     */

    @Serializable
    @JsonIgnoreUnknownKeys
    private data class RawBuildResponse(val builds: BuildPage)

    @Serializable
    @JsonIgnoreUnknownKeys
    private data class BuildPage(val data: List<BuildData>)

    @Serializable
    @JsonIgnoreUnknownKeys
    private data class BuildData(val uuid: Uuid, val installation: List<List<Installation>>)

    @Serializable
    @JsonIgnoreUnknownKeys
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
internal data class BuildResponse(val uuid: Uuid, val size: Long, val url: String) : Hashable {

    override fun toHashString(): String = "$uuid:$size"

}

/**
 * The response when querying the API for the configurations of a specific build.
 *
 * @property configs the configurations
 * @constructor Create a new Config response
 */
@Serializable
@JsonIgnoreUnknownKeys
internal data class ConfigResponse(val configs: List<Config>)

/**
 * Holds a configuration from the API.
 *
 * @property uuid the UUID of the configuration
 * @property data the actual configuration contents
 * @constructor Create a new Configuration
 */
@Serializable
@JsonIgnoreUnknownKeys
internal data class Config(
    @SerialName("value_uuid") val uuid: Uuid,
    @SerialName("location") val name: String,
    @SerialName("value") val data: String
)
