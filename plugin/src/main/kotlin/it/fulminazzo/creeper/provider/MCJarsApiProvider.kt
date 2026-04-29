package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ProjectInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.Serial
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
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