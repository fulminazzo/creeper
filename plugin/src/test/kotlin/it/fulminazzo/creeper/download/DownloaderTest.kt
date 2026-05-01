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
    private val downloader = Downloader.http()

    @Test
    fun `test that HTTP downloader sends correct User-Agent header`() {
        val port = 29126

        val requestCatcher = RequestCatcher().start(port)
        try {
            assertThrows<IOException> {
                downloader.download("http://localhost:$port$PATH", DESTINATION_PATH)
            }

            val lines = requestCatcher.getLines()
            assertContains(lines, "GET $PATH HTTP/1.1")
            assertContains(lines, "User-Agent: ${ProjectInfo.USER_AGENT}")

        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that request catcher works`() {
        val port = 29026

        val requestCatcher = RequestCatcher().start(port)
        try {
            val client = HttpClient.newHttpClient()

            assertThrows<IOException> {
                client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:$port$PATH")).build(),
                    HttpResponse.BodyHandlers.ofString()
                )
            }

            assertContains(requestCatcher.getLines(), "GET $PATH HTTP/1.1")
        } finally {
            requestCatcher.stop()
        }
    }

    companion object {
        private val DESTINATION_PATH = Path.of("build/resources/test/download/downloader_test.txt")
        private const val PATH = "/path/to/file"

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
