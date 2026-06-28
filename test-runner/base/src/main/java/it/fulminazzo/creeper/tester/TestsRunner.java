package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import it.fulminazzo.creeper.tester.util.ResourceUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
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
    public static final @NotNull String TEST_RESULTS_FILENAME = "test-results.json";

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
     * The results are represented by {@link TestResult}:
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
            TestResult testResult;
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
                testResult = TestResult.SuccessfulTestResult.of(summary);
            } catch (Throwable e) {
                logger.error("Error while running tests: {}", e.getMessage(), e);
                testResult = new TestResult.ThrowableResult(e);
            }
            try {
                String json = GSON.toJson(testResult);
                logger.info("Writing results to {}", resultsFile);
                Files.createDirectories(resultsFile.getParent());
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

}
