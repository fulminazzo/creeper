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

    /**
     * Gets the configuration file to extract necessary data to run the tests with.
     * Mainly two keys are used:
     * <ul>
     *     <li>{@code build-directory-path}: where the {@code build/} directory is located;</li>
     *     <li>{@code dependencies}: a list of JAR dependencies necessary for the tests to work.</li>
     * </ul>
     *
     * @return the configuration file
     */
    default @NotNull File configuration() {
        return new File(dataDirectory(), "config.yml");
    }

}
