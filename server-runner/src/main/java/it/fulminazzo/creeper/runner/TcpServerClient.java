package it.fulminazzo.creeper.runner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a TCP client connection.
 *
 * @see TcpServer
 */
final class TcpServerClient extends Thread implements InputListener, Closeable {
    private static long counter = 0;

    private final @NotNull Logger log;

    private final @NotNull InputProcessor inputProcessor;
    private final @NotNull OutputEmitter outputEmitter;

    private final @NotNull Socket client;
    private final @NotNull BufferedWriter output;

    /**
     * Instantiates a new Tcp server client.
     *
     * @param client the client
     */
    private TcpServerClient(
            final @NotNull InputProcessor inputProcessor,
            final @NotNull OutputEmitter outputEmitter,
            final @NotNull Socket client
    ) throws IOException {
        super(String.format("%s-%s (%s)",
                TcpServerClient.class.getSimpleName(),
                counter++,
                client.getInetAddress().getHostAddress() + ":" + client.getPort()
        ));
        this.log = Logger.getLogger(getName());

        this.inputProcessor = inputProcessor;
        this.outputEmitter = outputEmitter;

        this.client = client;
        this.output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    }

    @Override
    public void run() {
        log.info("New client initialized");
        inputProcessor.register(this);
        try (
                InputStreamReader reader = new InputStreamReader(client.getInputStream());
                BufferedReader input = new BufferedReader(reader)
        ) {
            String line;
            while ((line = input.readLine()) != null) outputEmitter.emit(line);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error reading from process", e);
        }
    }

    @Override
    public void processInput(final @NotNull String input) throws IOException {
        output.write(input);
        output.flush();
    }

    @Override
    public void close(final @Nullable String reason) {
        if (reason != null)
            try {
                processInput(reason);
            } catch (IOException ignored) {
                // do not log any error
            }
        close();
    }

    @Override
    public void close() {
        log.info("Client disconnecting");
        interrupt();
        inputProcessor.unregister(this);
        try {
            output.close();
        } catch (IOException ignored) {
            // do not log any error
        }
        try {
            client.close();
        } catch (IOException ignored) {
            // do not log any error
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Creates a new TCP server client.
     *
     * @param inputProcessor the input processor
     * @param outputEmitter  the output emitter
     * @param client         the actual client socket
     * @return the new TCP server client
     * @throws IOException if an error occurs while opening the streams of the client socket
     */
    public static @NotNull TcpServerClient of(
            final @NotNull InputProcessor inputProcessor,
            final @NotNull OutputEmitter outputEmitter,
            final @NotNull Socket client
    ) throws IOException {
        return new TcpServerClient(inputProcessor, outputEmitter, client);
    }

}
