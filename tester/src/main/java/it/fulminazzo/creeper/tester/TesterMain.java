package it.fulminazzo.creeper.tester;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class of the program, containing the testing and reporting logic.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TesterMain {
    private static final @NotNull String TEST_CLASSES_PACKAGE = TesterMain.class.getPackage().getName() + ".tests";
    private static final @NotNull String TEST_RESULTS_FILENAME = "test-results.json";

    @NotNull File workDir;

    /**
     * Executes the tests inside the {@link #TEST_CLASSES_PACKAGE} package.
     *
     * @param classLoader the class loader to get the classes from
     */
    public void runTests(final @NotNull ClassLoader classLoader) {
        try {
            Path resultsFile = Paths.get(workDir.getAbsolutePath(), TEST_RESULTS_FILENAME);
            Files.deleteIfExists(resultsFile);
            Files.createDirectories(resultsFile.getParent());
            //TODO: tests
        } catch (Exception e) {
            //TODO: exception handling
        }
    }

}
