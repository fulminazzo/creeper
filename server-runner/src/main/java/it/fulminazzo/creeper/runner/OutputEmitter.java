package it.fulminazzo.creeper.runner;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Represents an entity that can emit output as a string.
 * The output may be directed to an external source, such as a process or a file.
 */
public interface OutputEmitter {

    /**
     * Emits the output.
     *
     * @param output the output to emit
     * @throws IOException if an I/O error occurs
     */
    void emit(final @NotNull String output) throws IOException;

}
