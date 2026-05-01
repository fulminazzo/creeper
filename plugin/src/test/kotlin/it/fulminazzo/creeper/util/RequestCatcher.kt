package it.fulminazzo.creeper.util

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CompletableFuture

class RequestCatcher {
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