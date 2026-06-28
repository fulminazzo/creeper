package it.fulminazzo.creeper.tester;

import org.jetbrains.annotations.NotNull;

/**
 * A scheduler for running tests.
 * 
 * @see TestRunner
 */
public interface TestWorker {

    /**
     * Schedules the runnable to be executed.
     * 
     * @param runnable the runnable to execute
     */
    void schedule(final @NotNull Runnable runnable);
    
}
