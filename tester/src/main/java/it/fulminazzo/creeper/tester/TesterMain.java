package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class of the program, containing the testing and reporting logic.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TesterMain {
    private static final @NotNull String TEST_CLASSES_PACKAGE = TesterMain.class.getPackage().getName() + ".tests";
    private static final @NotNull String TEST_RESULTS_FILENAME = "test-results.json";

    private static final @NotNull Gson GSON = new Gson();

    @NotNull File workDir;
    @NotNull Logger logger;

    /**
     * Executes the tests inside the {@link #TEST_CLASSES_PACKAGE} package.
     *
     * @param classLoader the class loader to get the classes from
     */
    public void runTests(final @NotNull ClassLoader classLoader) {
        final TestsResult testsResult;
        final Path resultsFile = Paths.get(workDir.getAbsolutePath(), TEST_RESULTS_FILENAME);
        try {
            logger.info("Running tests...");
            Files.deleteIfExists(resultsFile);
            Files.createDirectories(resultsFile.getParent());
            throw new UnsupportedOperationException("Not yet implemented");
        } catch (Exception e) {
            logger.error("Error while running tests: {}", e.getMessage(), e);
            testsResult = new ExceptionResult(e);
        }
        try {
            String json = GSON.toJson(testsResult);
            logger.info("Writing results to {}", resultsFile);
            try (FileWriter writer = new FileWriter(resultsFile.toFile())) {
                writer.write(json);
            }
        } catch (Exception e) {
            // Yet another exception, we cannot recover from this one
            logger.error("Error while writing results file in {}: {}", resultsFile, e.getMessage(), e);
        }
    }

    /**
     * General DTO class to report tests results.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    abstract static class TestsResult {
        boolean success;

    }

    /**
     * DTO test result to report exceptions while preparing the environment for tests execution.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    static final class ExceptionResult extends TestsResult {
        @NotNull ExceptionData exception;

        /**
         * Instantiates a new Exception result.
         *
         * @param exception the exception that caused the failure
         */
        public ExceptionResult(final @NotNull Exception exception) {
            setSuccess(false);
            this.exception = ExceptionData.of(exception);
        }

    }

    /**
     * Contains all relevant information about an exception.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    static final class ExceptionData {
        @NotNull String exceptionName;
        @Nullable String message;
        @NotNull List<String> stackTrace;
        @Nullable ExceptionData cause;

        /**
         * Generates an {@link ExceptionData} from an exception.
         *
         * @param exception the exception to generate the data from
         * @return the exception data
         */
        public static @NotNull ExceptionData of(final @NotNull Exception exception) {
            Throwable cause = exception.getCause();
            ExceptionData causeData = cause instanceof Exception ? of((Exception) cause) : null;
            return new ExceptionData(
                    exception.getClass().getCanonicalName(),
                    exception.getMessage(),
                    Arrays.stream(exception.getStackTrace()).map(Object::toString).collect(Collectors.toList()),
                    causeData
            );
        }

    }

}
