package it.fulminazzo.creeper.util

import it.fulminazzo.creeper.ProjectInfo
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * A collection of utilities for HTTP requests.
 */
class HttpUtils private constructor() {

    companion object {
        private val CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(30.seconds.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        /**
         * Executes an HTTP request to the REST API at the given [url] and returns the raw response body.
         *
         * @param url the url
         * @return the body (or `null` if the resource was not found)
         * @throws ApiException if the API returns an error
         */
        internal fun getApi(url: String): CompletableFuture<String?> = CompletableFuture.supplyAsync {
            val request = HttpRequest.newBuilder()
                .header("User-Agent", ProjectInfo.USER_AGENT)
                .uri(URI.create(url))
                .build()
            val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
            when (response.statusCode()) {
                200 -> response.body()
                404 -> null
                else -> throw ApiException(response.statusCode())
            }
        }

    }

    /**
     * Exception thrown on unknown response code while querying the API.
     *
     * @constructor Create an empty Api exception
     *
     * @param statusCode the status code
     */
    class ApiException internal constructor(statusCode: Int) : Exception("Unexpected response code: $statusCode")

}