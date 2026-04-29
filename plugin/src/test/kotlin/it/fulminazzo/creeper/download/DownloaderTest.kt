package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.ProjectInfo
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains

class DownloaderTest {
    private val destinationPath = Path.of("build/resources/test/downloader/http_downloader_test.txt")

    @Test
    fun `test that HTTP downloader works`() {
        val downloader = Downloader.http()
        destinationPath.deleteIfExists()

        downloader.download("https://raw.githubusercontent.com/gradle/gradle/master/gradle.properties", destinationPath)
        assertContains(destinationPath.readText(), "org.gradle.jvmargs=")
    }

    @Test
    fun `test that HTTP downloader sends correct User-Agent header`() {
        val port = 29126
        val path = "/test/path"

        val requestCatcher = RequestCatcher().start(port)
        try {
            val downloader = Downloader.http()
            assertThrows<IOException> {
                downloader.download("http://localhost:$port$path", destinationPath)
            }

            val lines = requestCatcher.getLines()
            assertContains(lines, "GET $path HTTP/1.1")
            assertContains(lines, "User-Agent: ${ProjectInfo.NAME}/${ProjectInfo.VERSION}")

        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that request catcher works`() {
        val port = 29026
        val path = "/test/path"

        val requestCatcher = RequestCatcher().start(port)
        try {
            val client = HttpClient.newHttpClient()

            assertThrows<IOException> {
                client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).build(),
                    HttpResponse.BodyHandlers.ofString()
                )
            }

            assertContains(requestCatcher.getLines(), "GET $path HTTP/1.1")
        } finally {
            requestCatcher.stop()
        }
    }

}

private class RequestCatcher {
    private var server: ServerSocket? = null
    private var request: CompletableFuture<List<String>>? = null

    fun getLines(): List<String> = request!!.join()

    fun start(port: Int): RequestCatcher {
        server = ServerSocket(port)
        request = CompletableFuture.supplyAsync {
            val client = server!!.accept()
            val lines = handle(client)
            client.close()

            server?.close()
            server = null

            return@supplyAsync lines
        }
        return this
    }

    fun stop() {
        server?.close()
        server = null

        request?.cancel(true)
        request = null
    }

    private fun handle(client: Socket): List<String> {
        val stream = client.getInputStream().bufferedReader()
        val lines = mutableListOf<String>()
        while (true) {
            val line = stream.readLine() ?: break
            if (line.isBlank()) break
            lines.add(line)
        }
        return lines
    }

}
