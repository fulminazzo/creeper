package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.ProjectInfo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class DownloaderTest {
    private val downloader = Downloader.http()

    @BeforeEach
    fun setup() {
        DESTINATION_PATH.deleteIfExists()
        DESTINATION_PATH.parent.createDirectories()
    }

    @Test
    fun `test that HTTP downloader with directory path correctly stores file with content disposition header present`() {
        val port = getPort()

        val requestCatcher = RequestCatcher()
        requestCatcher.requestHandler = { client ->
            client.outputStream.write(
                """HTTP/1.1 200 OK
                |Content-Disposition: attachment; filename="downloader_test.txt"
                |
                |Hello world!
            """.trimMargin().toByteArray()
            )
        }
        requestCatcher.start(port)
        try {
            downloader.downloadIn("http://localhost:$port$PATH", DESTINATION_PATH.parent)
            assertTrue(
                DESTINATION_PATH.exists(),
                "Destination file does not exist: $DESTINATION_PATH"
            )
            assertContains(
                DESTINATION_PATH.readText(),
                "Hello world!"
            )
        } finally {
            requestCatcher.stop()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "attachment;",
            "attachment; filename=;",
            "attachment; filename=invalid;",
            "attachment; filename=\"\";",
        ]
    )
    fun `test that HTTP downloader with directory path correctly stores file even without content disposition header present`(
        header: String
    ) {
        val port = getPort()

        val requestCatcher = RequestCatcher()
        requestCatcher.requestHandler = { client ->
            val outputStream = client.outputStream
            outputStream.write("HTTP/1.1 200 OK \n".toByteArray())
            if (header.isNotBlank()) outputStream.write("Content-Disposition: $header\n".toByteArray())
            outputStream.write("\nHello world!".toByteArray())
        }
        requestCatcher.start(port)
        try {
            downloader.downloadIn("http://localhost:$port$PATH", DESTINATION_PATH.parent)
            assertTrue(
                DESTINATION_PATH.exists(),
                "Destination file does not exist: $DESTINATION_PATH"
            )
            assertContains(
                DESTINATION_PATH.readText(),
                "Hello world!"
            )
        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that HTTP downloader with directory path throws if it could not find the file name`() {
        val port = getPort()

        val requestCatcher = RequestCatcher()
        requestCatcher.requestHandler = { client ->
            val outputStream = client.outputStream
            outputStream.write("HTTP/1.1 200 OK \n".toByteArray())
            outputStream.write("\nHello world!".toByteArray())
        }
        requestCatcher.start(port)
        try {
            assertThrows<IllegalArgumentException> {
                downloader.downloadIn("http://localhost:$port/something/invalid/", DESTINATION_PATH.parent)
            }
        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that HTTP downloader sends correct User-Agent header`() {
        val port = getPort()

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
        val port = getPort()

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
        private const val PATH = "/path/to/file/downloader_test.txt"

        private var port = 29026

        fun getPort(): Int = port++

    }

}

private class RequestCatcher {
    var requestHandler: ((Socket) -> Unit)? = null
    private var server: ServerSocket? = null
    private var request: CompletableFuture<List<String>>? = null

    fun getLines(): List<String> = request!!.join()

    fun start(port: Int): RequestCatcher {
        server = ServerSocket(port)
        request = CompletableFuture.supplyAsync {
            val client = server!!.accept()
            val lines = handle(client)
            requestHandler?.invoke(client)
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
