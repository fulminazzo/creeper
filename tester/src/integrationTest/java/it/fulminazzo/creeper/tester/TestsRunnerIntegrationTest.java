package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class TestsRunnerIntegrationTest {
    /**
     * Java - JUnit
     * Java - TestNG
     * Groovy - Spock
     * Kotlin - JUnit
     */
    private static final int EXPECTED_TOTAL_TESTS = 4;

    private static final @NotNull ClassLoader CLASS_LOADER = TestsRunnerIntegrationTest.class.getClassLoader();

    private static final @NotNull File WORKING_DIR = new File("build/resources/test/integration_test");
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TestsRunnerIntegrationTest.class);

    private static final @NotNull Gson GSON = new Gson();

    @Test
    void testThatTestRunnerLoadsTestsFromDifferentPlatforms() throws IOException {
        TestsRunner runner = new TestsRunner(WORKING_DIR, LOGGER);
        assertDoesNotThrow(() -> runner.runTests(CLASS_LOADER));

        File resultsFile = new File(WORKING_DIR, TestsRunner.TEST_RESULTS_FILENAME);
        assertTrue(resultsFile.exists(), "Results file should have been created");

        try (FileReader reader = new FileReader(resultsFile)) {
            TestsRunner.SuccessfulTestsResult result = GSON.fromJson(reader, TestsRunner.SuccessfulTestsResult.class);
            assertTrue(result.isSuccess(), "Test should have not failed");

            assertEquals(0, result.getFailedContainers(), "There should have been no failed containers: " + result);
            assertEquals(0, result.getSkippedContainers(), "There should have been no skipped containers: " + result);

            assertEquals(0, result.getFailedTests(), "There should have been no failed tests: " + result);
            assertEquals(0, result.getSkippedTests(), "There should have been no skipped tests: " + result);
            assertEquals(
                    EXPECTED_TOTAL_TESTS,
                    result.getSucceededTests(),
                    String.format("There should have been %s succeeded tests: %s", EXPECTED_TOTAL_TESTS, result)
            );
            assertEquals(
                    EXPECTED_TOTAL_TESTS,
                    result.getTotalTests(),
                    String.format("There should have been %s total tests: %s", EXPECTED_TOTAL_TESTS, result)
            );
        }
    }

    @Test
    void testThatTestingEnvironmentWorks() {
        assertTrue(true);
    }

}
