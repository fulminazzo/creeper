package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.fulminazzo.creeper.tester.util.ResourceUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestsRunnerIntegrationTest {
    /**
     * Java - JUnit
     * Java - TestNG
     * Groovy - Spock
     * Kotlin - JUnit
     * Kotlin - Kotest
     * Scala - Scalatest
     * Scala - MUnit
     */
    private static final int EXPECTED_SUCCEEDED_TESTS = 7;
    // Scalatest counts the suite class as a test
    private static final int EXPECTED_TOTAL_TESTS = EXPECTED_SUCCEEDED_TESTS + 1;

    private static final @NotNull ClassLoader CLASS_LOADER = TestsRunnerIntegrationTest.class.getClassLoader();

    private static final @NotNull File WORKING_DIR = new File("build/resources/test/integration_test");
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TestsRunnerIntegrationTest.class);

    private static final @NotNull Gson GSON = new Gson();

    @Test
    void testThatTestRunnerLoadsTestsFromDifferentPlatforms() throws IOException {
        TestRunner runner = new TestRunner(Runnable::run, WORKING_DIR, LOGGER);
        assertDoesNotThrow(() -> runner.runTests(CLASS_LOADER));

        File resultsFile = new File(WORKING_DIR, TestRunner.TEST_RESULTS_FILENAME);
        assertTrue(resultsFile.exists(), "Results file should have been created");

        try (FileReader reader = new FileReader(resultsFile)) {
            Map<String, TestResult.SuccessfulTestResult> results = GSON.fromJson(
                    reader,
                    new TypeToken<Map<String, TestResult.SuccessfulTestResult>>() {
                    }.getType()
            );

            List<Class<?>> testClasses = ResourceUtils.loadClasses(
                    CLASS_LOADER,
                    TestRunner.class.getPackage().getName() + ".test"
            );

            assertEquals(
                    testClasses.size(),
                    results.size(),
                    "There should have been the same number of results as test classes: " + results
            );
            assertEquals(
                    EXPECTED_TOTAL_TESTS,
                    results.size(),
                    String.format("There should have been %s succeeded tests", EXPECTED_TOTAL_TESTS)
            );

            for (Class<?> testClass : testClasses) {
                String testClassName = testClass.getName();
                if (testClassName.endsWith("$1$1")) continue;
                TestResult.SuccessfulTestResult result = results.get(testClassName);
                assertNotNull(result, "Test result should not be null for test class: " + testClassName);
                assertTrue(result.isSuccess(), "Test should have not failed");

                assertEquals(0, result.getFailedContainers(), "There should have been no failed containers: " + result);
                assertEquals(0, result.getSkippedContainers(), "There should have been no skipped containers: " + result);

                assertEquals(0, result.getFailedTests(), "There should have been no failed tests: " + result);
                assertEquals(0, result.getSkippedTests(), "There should have been no skipped tests: " + result);

                int succeededTests = 1;
                assertEquals(
                        succeededTests,
                        result.getSucceededTests(),
                        String.format("There should have been %s succeeded tests: %s (%s)", succeededTests, result, testClassName)
                );

                int totalTests = 1;
                if (testClassName.endsWith("ScalaScalatestTest")) totalTests++;
                assertEquals(
                        totalTests,
                        result.getTotalTests(),
                        String.format("There should have been %s total tests: %s (%s)", totalTests, result, testClassName)
                );
                
            }
        }
    }

    @Test
    void testThatTestingEnvironmentWorks() {
        assertTrue(true);
    }

}
