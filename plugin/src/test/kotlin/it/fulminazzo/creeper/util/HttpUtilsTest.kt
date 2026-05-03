package it.fulminazzo.creeper.util

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpUtilsTest {
    private val requestCatcher = RequestCatcher(EXECUTOR)

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
            val response = HttpUtils.getApi("http://localhost:$port/test")
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
            val response = HttpUtils.getApi("http://localhost:$port/test")
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
            assertThrows<HttpUtils.ApiException> { HttpUtils.getApi("http://localhost:$port/test") }
        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that postApi returns correct response on 200`() {
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
            val response = HttpUtils.postApi("http://localhost:$port/test", "")
            assertEquals("test", response)
        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that postApi returns null on 404`() {
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
            val response = HttpUtils.postApi("http://localhost:$port/test", "")
            assertEquals(null, response)
        } finally {
            requestCatcher.stop()
        }
    }

    @Test
    fun `test that postApi throws on unexpected status code`() {
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
            assertThrows<HttpUtils.ApiException> { HttpUtils.postApi("http://localhost:$port/test", "") }
        } finally {
            requestCatcher.stop()
        }
    }

    private companion object {
        private val EXECUTOR = Executors.newCachedThreadPool()

        private var port = 10026

        fun getPort() = port++

        @JvmStatic
        @AfterAll
        fun tearDown() {
            EXECUTOR.shutdown()
        }

    }

}