package it.fulminazzo.creeper

import it.fulminazzo.creeper.ServerConnectorTest.Companion.SERVER_PORT
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ServerConnectorTest {
    private lateinit var server: ServerSocket

    private val clientLines = mutableListOf<String>()
    private lateinit var client: Socket
    private lateinit var thread: Thread

    private lateinit var connector: ServerConnector

    @BeforeEach
    fun setup() {
        server = ServerSocket(SERVER_PORT)

        thread = Thread {
            client = server.accept()
            try {
                val output = client.outputStream.bufferedWriter()
                client.inputStream.bufferedReader().forEachLine { line ->
                    if (line == "Hello") {
                        output.write("World")
                        output.flush()
                    }
                    clientLines.add(line)
                }
            } catch (_: IOException) {
                // ignore
            }
        }
        thread.start()

        connector = ServerConnector(SERVER_PORT)
    }

    @AfterEach
    fun tearDown() {
        try {
            if (connector.connected) connector.disconnect()
        } catch (_: UninitializedPropertyAccessException) {
            // ignore
        }
        try {
            thread.interrupt()
        } catch (_: UninitializedPropertyAccessException) {
            // ignore
        }
        try {
            client.close()
        } catch (_: UninitializedPropertyAccessException) {
            // ignore
        }
        try {
            server.close()
        } catch (_: UninitializedPropertyAccessException) {
            // ignore
        }
    }

    @Test
    fun `test that awaitInput is able to retrieve response from server`() {
        connector.connect()
        connector.send("Hello")
        val response = connector.awaitInput(
            """.*World.*""".toRegex(),
            1.seconds
        )
        assertTrue(response, "Response should have been true")
    }

    @Test
    fun `test that send works`() {
        connector.connect()
        connector.send("Hello")
        Thread.sleep(500)
        assertTrue(clientLines.contains("Hello"), "Client did not receive message")
    }

    @Test
    fun `test that connect and disconnect work`() {
        assertFalse(connector.connected, "Connector should not be connected")
        connector.connect()
        assertTrue(connector.connected, "Connector should be connected")
        connector.disconnect()
        assertFalse(connector.connected, "Connector should not be connected")
    }

    @Test
    fun `test that failed connect does not throw`() {
        val connector = ServerConnector(12345)
        connector.connect()
        assertFalse(connector.connected, "Connector should not be connected")
    }

    @ParameterizedTest
    @ValueSource(ints = [SERVER_PORT, 12345])
    fun `test that isServerOnline works`(port: Int) {
        assertEquals(
            port == SERVER_PORT,
            ServerConnector.isServerOnline(port)
        )
    }

    private companion object {
        private const val SERVER_PORT = 22056

    }

}