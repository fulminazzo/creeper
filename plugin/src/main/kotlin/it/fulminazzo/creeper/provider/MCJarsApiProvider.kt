package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.Serial
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * A [MinecraftJarProvider] that uses the [MCJars API](https://mcjars.app).
 */
class MCJarsApiProvider {
    private val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(30.seconds.toJavaDuration())
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    /**
     * Gets the requested build information.
     *
     * @param type the platform
     * @param version the version of the build
     * @return the build information (or `null` if the build was not found)
     */
    fun getBuild(type: ServerType.MinecraftType, version: String): BuildResponse? {
        @Serializable
        data class Installation(val url: String, val size: Long)

        @Serializable
        data class BuildData(val uuid: String, val installations: List<Installation>)

        @Serializable
        data class Build(val data: List<BuildData>)

        @Serializable
        data class RawBuildResponse(val builds: Build)

        val url = getBuildUrl(type, version)
        return getFromApi(url)
            ?.let { Json.decodeFromString<RawBuildResponse>(it) }
            ?.let {
                val firstBuildData = it.builds.data.first()
                val firstInstallation = firstBuildData.installations.first()
                BuildResponse(
                    UUID.fromString(firstBuildData.uuid),
                    firstInstallation.size,
                    firstInstallation.url
                )
            }
    }

    /**
     * Executes a GET request to the given [url] and returns the response body.
     *
     * @param url the url
     * @return the body (or `null` if the resource was not found)
     */
    private fun getFromApi(url: String): String? {
        val request = HttpRequest.newBuilder()
            .header("User-Agent", ProjectInfo.USER_AGENT)
            .uri(URI.create("$API_URL$url"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return when (response.statusCode()) {
            200 -> response.body()
            404 -> null
            else -> {
                val error = Json.decodeFromString<ErrorResponse>(response.body())
                throw ApiException(response.statusCode(), error)
            }
        }
    }

    companion object {
        private const val API_URL = "https://mcjars.app/api/v3/"
        private const val BUILDS_URL = $$"builds/types/%1$s/versions/%2$s"

        /**
         * Gets the URL to get the build information for the given [type] and [version].
         *
         * @param type the platform
         * @param version the version of the build
         * @return the URL
         */
        private fun getBuildUrl(type: ServerType.MinecraftType, version: String): String =
            BUILDS_URL.format(type.name.uppercase(), version)

    }

    /**
     * Exception thrown on unknown response code while querying the API.
     *
     * @constructor Create an empty Api exception
     *
     * @param statusCode the status code
     * @param response the response from the API
     */
    private class ApiException(statusCode: Int, response: ErrorResponse) : Exception(
        "Unexpected response code: $statusCode. Errors: ${response.errors.joinToString(", ")}"
    ) {

        companion object {
            @Serial
            private const val serialVersionUID: Long = -2206243090390470112L

        }

    }

    /**
     * Identifies an error response from the API.
     *
     * @property errors the list of errors
     * @constructor Create a new Error response
     */
    @Serializable
    private data class ErrorResponse(val errors: List<String>)

}

/**
 * The build response when querying the API.
 *
 * @property uuid the UUID of the build
 * @property size the size of the build
 * @property url the URL to download the build
 * @constructor Create a new Build response
 */
data class BuildResponse(val uuid: UUID, val size: Long, val url: String)
