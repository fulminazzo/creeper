package it.fulminazzo.creeper.tester;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

/**
 * Marker interface for the application running the tester.
 */
public interface TesterApplication {

    /**
     * Gets the logger of the application.
     * 
     * @return the logger
     */
    @NotNull Logger logger();

    /**
     * Gets the directory where the application stores its data.
     * 
     * @return the data directory
     */
    @NotNull File dataDirectory();
    
}
