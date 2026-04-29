package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.ProjectInfo
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
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
     * Downloads the requested resource.
     *
     * @param resource the resource path on the web
     * @param destination the destination path where it will be stored
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
    private class HttpDownloader : Downloader {
        private val client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(30.seconds.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        override fun download(resource: String, destination: Path) {
            destination.createParentDirectories()
            destination.deleteIfExists()
            destination.createFile()

            val request = HttpRequest.newBuilder()
                .header("User-Agent", USER_AGENT)
                .uri(URI.create(resource))
                .build()

            client.send(request, HttpResponse.BodyHandlers.ofFile(destination))
        }

        companion object {
            const val USER_AGENT = "${ProjectInfo.NAME}/${ProjectInfo.VERSION}"

        }

    }

}