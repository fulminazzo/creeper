package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.ProjectInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains

class DownloaderTest {
    private val downloader = Downloader.http()

    @Test
    fun `test that HTTP downloader sends correct User-Agent header`() {
        val port = 29126

        runBlocking {
            val requestCatcher = RequestCatcher(this).start(port)
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
    }

    @Test
    fun `test that request catcher works`() {
        val port = 29026

        runBlocking {
            val requestCatcher = RequestCatcher(this).start(port)
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
    }

    companion object {
        private val DESTINATION_PATH = Path.of("build/resources/test/downloader/downloader_test.txt")
        private const val PATH = "/path/to/file"

    }

}

private class RequestCatcher(private val scope: CoroutineScope) {
    private var server: ServerSocket? = null
    private var request: Deferred<List<String>>? = null

    suspend fun getLines(): List<String> = request?.await() ?: throw IllegalStateException("Request not started")

    fun start(port: Int): RequestCatcher {
        server = ServerSocket(port)
        request = scope.async(Dispatchers.IO) {
            try {
                server?.accept()?.use { handle(it) } ?: emptyList()
            } finally {
                server?.close()
                server = null
            }
        }
        return this
    }

    fun stop() {
        server?.close()
        server = null

        request?.cancel()
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
