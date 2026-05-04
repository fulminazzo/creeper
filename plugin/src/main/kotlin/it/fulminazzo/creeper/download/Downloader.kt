package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.ProjectInfo
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Function to download resources from the web.
 */
interface Downloader {

    /**
     * Attempts to get the filename of a resource.
     *
     * @param resource the resource
     * @return the filename, or `null` if it could not be derived
     * @throws UnrecognizedStatusCodeException in case of an unrecognized response from the server
     */
    fun getFileName(resource: String): String?

    /**
     * Downloads the requested resource.
     *
     * @param resource the resource path on the web
     * @param destination the destination path where it will be stored
     * @throws UnrecognizedStatusCodeException in case of an unrecognized response from the server
     */
    fun download(resource: String, destination: Path)

    companion object {

        /**
         * Creates a new [Downloader] that uses the HTTP protocol.
         *
         * @return the downloader
         */
        fun http(): Downloader = HttpDownloader()

    }

    /**
     * Base implementation of [Downloader] that uses the HTTP protocol.
     *
     * @constructor Create an empty Http downloader
     */
    class HttpDownloader : Downloader {

        override fun getFileName(resource: String): String? {
            val request = createRequest(resource)
            val response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding())
            return if (response.statusCode() == 404) null
            else if (response.statusCode() != 200) throw UnrecognizedStatusCodeException(
                response.statusCode(),
                resource
            )
            else computeFileName(resource, response)
        }

        override fun download(resource: String, destination: Path) {
            destination.createParentDirectories().deleteIfExists()
            destination.createFile()

            val request = createRequest(resource)
            val response = CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(destination))
            if (response.statusCode() != 200) throw UnrecognizedStatusCodeException(response.statusCode(), resource)
        }

        private fun createRequest(resource: String): HttpRequest? {
            val request = HttpRequest.newBuilder()
                .header("User-Agent", ProjectInfo.USER_AGENT)
                .uri(URI.create(resource))
                .build()
            return request
        }

        companion object {
            private val CLIENT = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(30.seconds.toJavaDuration())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()

            private const val CONTENT_DISPOSITION = "Content-Disposition"
            private val FILE_NAME_REGEX = "filename=\"(.+)\"".toRegex()

            /**
             * Computes the file name of a resource.
             * First it attempts to extract it from the `Content-Disposition` header.
             * If it fails, it falls back to the URL.
             *
             * @param url the url of the resource
             * @param response the response after requesting the resource
             * @return the name of the resource
             * @throws IllegalArgumentException if it could not derive the name of the resource
             */
            fun computeFileName(url: String, response: HttpResponse<*>): String {
                val contentDisposition = response.headers().firstValue(CONTENT_DISPOSITION)
                if (contentDisposition.isPresent) {
                    val header = contentDisposition.get()
                    val fileName = FILE_NAME_REGEX.find(header)?.groupValues?.get(1)
                    if (fileName != null) return fileName
                }
                return URI(url).path.substringAfterLast('/')
                    .takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Could not derive file name from $url")
            }

        }

    }

}

/**
 * An exception thrown by [Downloader.HttpDownloader] when the server returns an unexpected status code.
 *
 * @constructor Creates a new Unrecognized status code exception
 *
 * @param statusCode the status code returned by the server
 * @param resource the resource that was requested
 */
class UnrecognizedStatusCodeException(statusCode: Int, resource: String) :
    Exception("Unexpected response code while querying $resource: $statusCode")