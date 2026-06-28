package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import it.fulminazzo.creeper.tester.util.ResourceUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A runner for executing tests from the {@link #testClassesPackage}.
 * Check {@link #runTests(ClassLoader)} to understand how reports are computed.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class TestRunner {
    /**
     * The file where the test results are stored.
     */
    public static final @NotNull String TEST_RESULTS_FILENAME = "test-results.json";

    private static final @NotNull Gson GSON = new Gson();

    @NotNull TestWorker testWorker;

    @NotNull String testClassesPackage;
    @NotNull File workDir;
    @NotNull Logger logger;

    /**
     * Instantiates a new Test runner.
     *
     * @param testWorker the actual worker of the tests
     * @param workDir    the work dir
     * @param logger     the logger
     */
    public TestRunner(
            final @NotNull TestWorker testWorker,
            final @NotNull File workDir,
            final @NotNull Logger logger
    ) {
        this(testWorker, TestRunner.class.getPackage().getName() + ".test", workDir, logger);
    }

    /**
     * Executes the test classes in the {@link #testClassesPackage} package with the <b>JUnit</b> test launcher.
     * Then, it writes the results under {@link #workDir}/{@link #TEST_RESULTS_FILENAME}.
     * <br>
     * The results are represented by {@link TestResult}:
     * <ul>
     *     <li>if the {@code success} flag is {@code false}, it means an exception occurred
     *     while preparing the testing environment or gathering the results;</li>
     *     <li>if the {@code success} flag is {@code true}, it means the <b>execution</b>
     *     of tests was successful, but it <b>does not</b> ensure that all the tests passed.</li>
     * </ul>
     *
     * @param classLoader the class loader to get the classes from
     */
    public void runTests(final @NotNull ClassLoader classLoader) {
        final Map<String, TestResult> results = new ConcurrentHashMap<>();

        try {
            logger.info("Initializing tests launcher.");

            List<Class<?>> testClasses = ResourceUtils.loadClasses(classLoader, testClassesPackage);
            logger.info("Discovered {} test classes.", testClasses.size());

            logger.info("Initiating tests execution.");
            for (Class<?> testClass : testClasses)
                testWorker.schedule(() ->
                        results.put(testClass.getCanonicalName(), runSingleTest(classLoader, testClass))
                );
        } catch (IOException e) {
            logger.error("Error while running tests: {}", e.getMessage(), e);
            results.put("root", new TestResult.ThrowableResult(e));
        }

        testWorker.schedule(() -> {
            final Path resultsFile = Paths.get(workDir.getAbsolutePath(), TEST_RESULTS_FILENAME);
            try {
                String json = GSON.toJson(results);
                logger.info("Writing results to {}", resultsFile);
                Files.createDirectories(resultsFile.getParent());
                try (FileWriter writer = new FileWriter(resultsFile.toFile())) {
                    writer.write(json);
                }
            } catch (Exception e) {
                // Yet another exception, we cannot recover from this one
                logger.error("Error while writing results file in {}: {}", resultsFile, e.getMessage(), e);
            } finally {
                logger.info("Tests execution completed.");
            }
        });
    }

    /**
     * Executes a single test class with the <b>JUnit</b> test launcher.
     *
     * @param classLoader the class loader to get the classes from
     * @param testClass   the test class to run
     * @return the test result
     */
    @NotNull TestResult runSingleTest(final @NotNull ClassLoader classLoader, final @NotNull Class<?> testClass) {
        Thread currentThread = Thread.currentThread();
        ClassLoader previous = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(classLoader);

            logger.debug("Running test: {}", testClass.getCanonicalName());
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectClass(testClass))
                    .build();

            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
            launcher.registerTestExecutionListeners(summaryListener);
            launcher.execute(request);

            logger.debug("Finished running test: {}", testClass.getCanonicalName());
            logger.debug("Gathering results...");

            TestExecutionSummary summary = summaryListener.getSummary();
            return TestResult.SuccessfulTestResult.of(summary);
        } catch (Throwable e) {
            logger.error("Error while running test: {}", e.getMessage(), e);
            return new TestResult.ThrowableResult(e);
        } finally {
            currentThread.setContextClassLoader(previous);
        }
    }

}
