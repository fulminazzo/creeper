package it.fulminazzo.creeper.runner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Represents a listener for input from a general input source.
 *
 * @see InputProcessor
 */
public interface InputListener {

    /**
     * Processes the input.
     *
     * @param input the input to process
     * @throws IOException if an I/O error occurs
     */
    void processInput(final @NotNull String input) throws IOException;

    /**
     * Closes the input listener.
     *
     * @param reason the reason for closing the listener
     */
    void close(final @Nullable String reason);

}
