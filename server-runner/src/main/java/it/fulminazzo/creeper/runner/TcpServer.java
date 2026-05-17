package it.fulminazzo.creeper.runner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TCP server for forwarding inputs and outputs from the process to the clients.
 *
 * @see TcpServerClient
 */
public final class TcpServer extends Thread implements InputProcessor, InputListener, Closeable {
    private static long counter = 0;

    private final @NotNull Collection<InputListener> clients = Collections.synchronizedSet(new HashSet<>());

    private final @NotNull Logger log;

    private final @NotNull InputProcessor inputProcessor;
    private final @NotNull OutputEmitter outputEmitter;

    private final @NotNull ServerSocket server;

    /**
     * Instantiates a new Tcp server.
     *
     * @param inputProcessor the input processor
     * @param outputEmitter  the output emitter
     * @param server         the server socket
     */
    private TcpServer(
            final @NotNull InputProcessor inputProcessor,
            final @NotNull OutputEmitter outputEmitter,
            final @NotNull ServerSocket server
    ) {
        super(String.format("%s-%s", TcpServer.class.getSimpleName(), counter++));
        this.log = Logger.getLogger(getName());

        this.inputProcessor = inputProcessor;
        this.outputEmitter = outputEmitter;

        this.server = server;
    }

    @Override
    public void run() {
        try {
            while (!server.isClosed())
                try {
                    Socket clientSocket = server.accept();
                    log.info(String.format(
                            "New client connected: %s:%s",
                            clientSocket.getInetAddress().getHostAddress(),
                            clientSocket.getPort())
                    );
                    TcpServerClient client = TcpServerClient.of(
                            this,
                            outputEmitter,
                            clientSocket
                    );
                    client.start();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error accepting client connection", e);
                }
        } finally {
            close();
        }
    }

    @Override
    public void processInput(final @NotNull String input) {
        log.info(input);
    }

    @Override
    public void close(final @Nullable String reason) {
        if (reason != null) log.info(reason);
        close();
    }

    @Override
    public void register(final @NotNull InputListener listener) {
        clients.add(listener);
        inputProcessor.register(listener);
    }

    @Override
    public void unregister(final @NotNull InputListener listener) {
        clients.remove(listener);
        inputProcessor.unregister(listener);
    }

    @Override
    public void closeAll() {
        inputProcessor.closeAll();
    }

    @Override
    public void close() {
        closeAll();
        interrupt();
        inputProcessor.unregister(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Creates a new TCP server.
     *
     * @param inputProcessor the input processor
     * @param outputEmitter  the output emitter
     * @param port           the port to listen on
     * @return the new TCP server
     * @throws IOException if an error occurs while opening the server socket
     */
    public static @NotNull TcpServer of(
            final @NotNull InputProcessor inputProcessor,
            final @NotNull OutputEmitter outputEmitter,
            final int port
    ) throws IOException {
        return new TcpServer(inputProcessor, outputEmitter, new ServerSocket(port));
    }

}
