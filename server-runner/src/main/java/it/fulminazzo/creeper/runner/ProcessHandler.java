package it.fulminazzo.creeper.runner;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Wrapper for a {@link Process} to forward inputs to {@link InputListener}s and to forward outputs to the process.
 *
 * @see InputListener
 * @see InputProcessor
 * @see OutputEmitter
 */
@Log
public final class ProcessHandler implements InputProcessor, OutputEmitter {
    private final @NotNull Collection<InputListener> listeners = Collections.synchronizedSet(new HashSet<>());
    private final @NotNull Process process;

    private final @NotNull BufferedWriter output;

    private final @NotNull Future<Void> inputReader;

    /**
     * Instantiates a new Process handler.
     *
     * @param process the process
     */
    private ProcessHandler(final @NotNull Process process) {
        this.process = process;

        this.output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        this.inputReader = CompletableFuture.runAsync(
                () -> {
                    try (
                            InputStreamReader reader = new InputStreamReader(process.getInputStream());
                            BufferedReader input = new BufferedReader(reader)
                    ) {
                        String line;
                        while ((line = input.readLine()) != null) {
                            for (InputListener listener : listeners)
                                try {
                                    listener.processInput(line);
                                } catch (IOException e) {
                                    log.log(Level.WARNING, "Error processing input for listener " + listener, e);
                                }
                        }
                    } catch (IOException e) {
                        log.log(Level.SEVERE, "Error reading from process", e);
                    }
                }
        );
    }

    /**
     * Waits for the internal process to finish.
     */
    public void await() {
        try {
            process.waitFor();
        } catch (InterruptedException ignored) {
            // do nothing
        }
    }

    /**
     * Forcibly stops the internal process and closes all the listeners.
     */
    public synchronized void stop() {
        closeAll();
        inputReader.cancel(true);
        try {
            output.close();
        } catch (IOException ignored) {
            // do not log any closing error
        }
        process.destroy();
    }

    @Override
    public void register(final @NotNull InputListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregister(final @NotNull InputListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void closeAll() {
        listeners.forEach(l -> l.close("Internal process terminated"));
    }

    @Override
    public void emit(final @NotNull String output) throws IOException {
        this.output.write(output);
        this.output.flush();
    }

    /**
     * Creates a new Process handler.
     *
     * @param processBuilder the builder of the process to start
     * @return the new process handler
     * @throws IOException if an error occurs while starting the process
     */
    public static @NotNull ProcessHandler of(final @NotNull ProcessBuilder processBuilder) throws IOException {
        return new ProcessHandler(
                processBuilder
                        .redirectErrorStream(true)
                        .start()
        );
    }

}
