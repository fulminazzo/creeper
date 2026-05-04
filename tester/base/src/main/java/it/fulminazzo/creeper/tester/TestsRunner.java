package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import it.fulminazzo.creeper.tester.util.ResourceUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
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
 * A runner for executing tests from the {@link #testClassesPackage}.
 * Check {@link #runTests(ClassLoader)} to understand how reports are computed.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TestsRunner {
    /**
     * The Test results filename.
     */
    static final @NotNull String TEST_RESULTS_FILENAME = "test-results.json";

    private static final @NotNull Gson GSON = new Gson();

    @NotNull String testClassesPackage;
    @NotNull File workDir;
    @NotNull Logger logger;

    /**
     * Instantiates a new Tests runner.
     *
     * @param workDir the work dir
     * @param logger  the logger
     */
    public TestsRunner(final @NotNull File workDir, final @NotNull Logger logger) {
        this.testClassesPackage = TestsRunner.class.getPackage().getName() + ".tests";
        this.workDir = workDir;
        this.logger = logger;
    }

    /**
     * Executes the classes in the {@link #testClassesPackage} package with the <b>JUnit</b> test launcher.
     * Then, it writes the results under {@link #workDir}/{@link #TEST_RESULTS_FILENAME}.
     * <br>
     * The results are represented by {@link TestsResult}:
     * <ul>
     *     <li>if the {@code success} flag is {@code false}, it means an exception occurred
     *     while preparing the testing environment or gathering the results;</li>
     *     <li>if the {@code success} flag is {@code true}, it menas the <b>execution</b>
     *     of tests was successful, but it <b>does not</b> assure that all the tests passed.</li>
     * </ul>
     *
     * @param classLoader the class loader to get the classes from
     */
    public void runTests(final @NotNull ClassLoader classLoader) {
        Thread currentThread = Thread.currentThread();
        ClassLoader previous = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(classLoader);
            TestsResult testsResult;
            final Path resultsFile = Paths.get(workDir.getAbsolutePath(), TEST_RESULTS_FILENAME);
            try {
                logger.info("Running tests...");
                Files.deleteIfExists(resultsFile);
                Files.createDirectories(resultsFile.getParent());

                LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                        .selectors(ResourceUtils.loadClasses(classLoader, testClassesPackage).stream()
                                .map(DiscoverySelectors::selectClass)
                                .collect(Collectors.toList()))
                        .build();

                Launcher launcher = LauncherFactory.create();
                SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
                launcher.registerTestExecutionListeners(summaryListener);
                launcher.execute(request);

                TestExecutionSummary summary = summaryListener.getSummary();
                testsResult = SuccessfulTestsResult.of(summary);
            } catch (Throwable e) {
                logger.error("Error while running tests: {}", e.getMessage(), e);
                testsResult = new ThrowableResult(e);
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
        } finally {
            currentThread.setContextClassLoader(previous);
        }
    }

    /**
     * General DTO class to report tests results.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    abstract static class TestsResult {
        boolean success = true;

    }

    /**
     * DTO test result to report successful tests execution.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @Builder
    static final class SuccessfulTestsResult extends TestsResult {
        long timeStarted;
        long timeFinished;

        long failedContainers;
        long succeededContainers;
        long skippedContainers;
        long totalContainers;
        @NotNull List<Failure> containerFailures;

        long failedTests;
        long succeededTests;
        long skippedTests;
        long totalTests;
        @NotNull List<Failure> testFailures;

        /**
         * Generates a {@link SuccessfulTestsResult} from a JUnit {@link TestExecutionSummary}.
         *
         * @param summary the summary to generate the data from
         * @return the test results
         */
        public static @NotNull SuccessfulTestsResult of(final @NotNull TestExecutionSummary summary) {
            return SuccessfulTestsResult.builder()
                    .timeStarted(summary.getTimeStarted())
                    .timeFinished(summary.getTimeFinished())
                    .failedContainers(summary.getContainersFailedCount())
                    .succeededContainers(summary.getContainersSucceededCount())
                    .skippedContainers(summary.getContainersSkippedCount())
                    .totalContainers(summary.getContainersFoundCount())
                    .containerFailures(summary.getFailures().stream()
                            .filter(f -> f.getTestIdentifier().isContainer())
                            .map(Failure::of)
                            .collect(Collectors.toList())
                    )
                    .failedTests(summary.getTestsFailedCount())
                    .succeededTests(summary.getTestsSucceededCount())
                    .skippedTests(summary.getTestsSkippedCount())
                    .totalTests(summary.getTestsFoundCount())
                    .testFailures(summary.getFailures().stream()
                            .filter(f -> f.getTestIdentifier().isTest())
                            .map(Failure::of)
                            .collect(Collectors.toList())
                    )
                    .build();
        }

    }

    /**
     * Contains all relevant information about a failure, either test or container.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static final class Failure {
        @Nullable String displayName;
        @Nullable String message;
        @Nullable TestSource source;
        @NotNull TestsRunner.ThrowableData exception;

        /**
         * Extracts a {@link Failure} from a JUnit {@link TestExecutionSummary.Failure}.
         *
         * @param failure the failure to extract the data from
         * @return the failure data
         */
        public static @NotNull Failure of(final @NotNull TestExecutionSummary.Failure failure) {
            final TestIdentifier identifier = failure.getTestIdentifier();
            final Throwable exception = failure.getException();
            TestSource source = TestSource.of(identifier);
            return new Failure(identifier.getDisplayName(), exception.getMessage(), source, ThrowableData.of(exception));
        }

    }

    /**
     * Contains all relevant information about a test source (either class or method).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static final class TestSource {
        @NotNull String className;
        @Nullable String methodName;
        @Nullable String methodParameters;

        /**
         * Extracts the {@link TestSource} from a JUnit {@link TestIdentifier}.
         *
         * @param identifier the identifier to get the source from
         * @return {@code null} if there is no source or is not recognized
         */
        public static @Nullable TestSource of(final @NotNull TestIdentifier identifier) {
            return identifier.getSource().map(source -> {
                if (source instanceof MethodSource) {
                    MethodSource methodSource = (MethodSource) source;
                    return new TestSource(methodSource.getClassName(), methodSource.getMethodName(), methodSource.getMethodParameterTypes());
                } else if (source instanceof ClassSource) {
                    ClassSource classSource = (ClassSource) source;
                    return new TestSource(classSource.getClassName(), null, null);
                } else return null;
            }).orElse(null);
        }
    }

    /**
     * DTO test result to report exceptions while preparing the environment for tests execution.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    static final class ThrowableResult extends TestsResult {
        @NotNull TestsRunner.ThrowableData exception;

        /**
         * Instantiates a new Throwable result.
         *
         * @param exception the exception that caused the failure
         */
        public ThrowableResult(final @NotNull Throwable exception) {
            setSuccess(false);
            this.exception = ThrowableData.of(exception);
        }

    }

    /**
     * Contains all relevant information about a {@link Throwable}.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    static final class ThrowableData {
        @NotNull String throwableName;
        @Nullable String message;
        @NotNull List<String> stackTrace;
        @Nullable TestsRunner.ThrowableData cause;

        /**
         * Generates an {@link ThrowableData} from a {@link Throwable}.
         *
         * @param throwable the throwable to generate the data from
         * @return the throwable data
         */
        public static @NotNull ThrowableData of(final @NotNull Throwable throwable) {
            Throwable cause = throwable.getCause();
            ThrowableData causeData = cause != null ? of(cause) : null;
            return new ThrowableData(
                    throwable.getClass().getCanonicalName(),
                    throwable.getMessage(),
                    Arrays.stream(throwable.getStackTrace()).map(Object::toString).collect(Collectors.toList()),
                    causeData
            );
        }

    }

}
