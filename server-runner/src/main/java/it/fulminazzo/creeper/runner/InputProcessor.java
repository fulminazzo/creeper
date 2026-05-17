package it.fulminazzo.creeper.runner;

import org.jetbrains.annotations.NotNull;

/**
 * An object for processing inputs.
 *
 * @see InputListener
 */
public interface InputProcessor {

    /**
     * Registers a new listener.
     *
     * @param listener the listener
     */
    void register(final @NotNull InputListener listener);

    /**
     * Unregisters the listener.
     *
     * @param listener the listener
     */
    void unregister(final @NotNull InputListener listener);

    /**
     * Closes all the listeners.
     */
    void closeAll();

}
