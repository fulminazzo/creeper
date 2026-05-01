package it.fulminazzo.creeper.util

import it.fulminazzo.creeper.util.HttpUtils.Companion.getApi
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CompletionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HttpUtilsTest {
    private val requestCatcher = RequestCatcher()

    @Test
    fun `test that getApi returns correct response on 200`() {
        val port = getPort()
        requestCatcher.start(port)
        try {
            requestCatcher.requestHandler = { client ->
                client.outputStream.write(
                    """HTTP/1.1 200 OK
                |Content-Type: text/plain
                |
                |test""".trimMargin().toByteArray()
                )
            }
            val response = getApi("http://localhost:$port/test").join()
            assertEquals("test", response)
        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that getApi returns null on 404`() {
        val port = getPort()
        requestCatcher.start(port)
        try {
            requestCatcher.requestHandler = { client ->
                client.outputStream.write(
                    """HTTP/1.1 404 Bad Request
                |Content-Type: text/plain
                |
                |test""".trimMargin().toByteArray()
                )
            }
            val response = getApi("http://localhost:$port/test").join()
            assertEquals(null, response)
        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that getApi throws on unexpected status code`() {
        val port = getPort()
        requestCatcher.start(port)
        try {
            requestCatcher.requestHandler = { client ->
                client.outputStream.write(
                    """HTTP/1.1 500 Internal Server Error
                |Content-Type: text/plain
                |
                |test""".trimMargin().toByteArray()
                )
            }
            val e = assertThrows<CompletionException> { getApi("http://localhost:$port/test").join() }
            assertIs<ApiException>(e.cause)
        } finally {
            requestCatcher.stop()
        }
    }

    private companion object {
        private var port = 10026

        fun getPort() = port++

    }

}