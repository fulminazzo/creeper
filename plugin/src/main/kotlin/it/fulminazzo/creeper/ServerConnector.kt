package it.fulminazzo.creeper

import it.fulminazzo.creeper.util.VerifyUtils
import java.io.IOException
import java.net.Socket
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Connector for interacting with the TCP server linked to a running Minecraft server.
 *
 * @property port the port of the TCP server
 * @property host the address of the TCP server
 * @constructor Creates a new Server connector
 */
class ServerConnector(
    private val port: Int,
    private val host: String = "127.0.0.1",
) {
    private val lines = mutableListOf<String>()

    private var socket: Socket? = null
    private var lineReader: CompletableFuture<Void>? = null

    /**
     * Checks if the connector is connected.
     *
     * @return `true` if it is
     */
    val connected: Boolean
        get() = socket?.isClosed?.let { !it } ?: false

    /**
     * Awaits that the requested input appears once in the input stream.
     *
     * @param regex the regular expression to validate the input
     * @param timeout the timeout after which the input will be considered non-arrived
     * @param interval the interval between each check
     * @return `true` if the input was read at least once
     */
    fun awaitInput(regex: Regex, timeout: Duration, interval: Duration = 1.seconds): Boolean {
        // Going back of one in case the requested input is already present.
        val start = (lines.size - 2).takeIf { it >= 0 } ?: 0
        return VerifyUtils.awaitVerified(
            {
                lines.subList(start, lines.size).any { regex.matches(it) }
            },
            timeout,
            interval = interval
        )
    }

    /**
     * Sends data to the TCP server.
     *
     * @param data the data to send
     */
    fun send(data: String) {
        check(!connected) { "Server connector is not connected to the server" }
        socket?.outputStream?.bufferedWriter()?.use { writer ->
            writer.write(data)
            writer.newLine()
            writer.flush()
        }
    }

    /**
     * Connects to the TCP server.
     * Users should ALWAYS check with [connected] if the connection was successful.
     */
    fun connect() {
        check(connected) { "Server connector is already connected to the server" }
        try {
            socket = Socket(host, port)
            lineReader = CompletableFuture.runAsync {
                socket?.inputStream?.bufferedReader()?.use { reader ->
                    reader.forEachLine { lines += it }
                }
            }
        } catch (_: IOException) {
            // could not connect to the server, resetting fields
            lineReader?.cancel(true)
            lineReader = null
            socket = null
        }
    }

    /**
     * Disconnects from the TCP server.
     */
    fun disconnect() {
        check(!connected) { "Server connector is not connected to the server" }
        lineReader?.cancel(true)
        try {
            socket?.close()
        } catch (_: IOException) {
            // ignore errors
        }
        socket = null
    }

    companion object {

        /**
         * Checks if the TCP server is online.
         *
         * @param port the port of the TCP server
         * @param host the address of the TCP server
         * @return `true` if the server is online
         */
        fun isServerOnline(port: Int, host: String = "127.0.0.1"): Boolean {
            val connector = ServerConnector(port, host)
            connector.connect()
            val isOnline = connector.connected
            connector.disconnect()
            return isOnline
        }

    }

}