package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.fulminazzo.creeper.tester.util.ResourceUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestRunnerIntegrationTest {
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

    private static final List<File> TEST_SOURCES = Stream.of("groovy", "java", "kotlin", "scala")
            .map(c -> String.format("build/classes/%s/functionalTest", c))
            .map(File::new)
            .collect(Collectors.toList());

    private static final @NotNull File WORKING_DIR = new File("build/resources/test/integration_test");
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TestRunnerIntegrationTest.class);

    private static final @NotNull Gson GSON = new Gson();

    private static URLClassLoader classLoader;

    @BeforeAll
    static void setUpAll() {
        String[] dependenciesPaths = System.getProperty("functionaltest.framework.classpath").split(File.pathSeparator);

        classLoader = new URLClassLoader(
                Stream.concat(
                                TEST_SOURCES.stream(),
                                Arrays.stream(dependenciesPaths).map(File::new)
                        )
                        .map(File::toURI)
                        .map(f -> {
                            try {
                                return f.toURL();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toArray(URL[]::new),
                TestRunnerIntegrationTest.class.getClassLoader()
        );
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        classLoader.close();
    }

    @Test
    void testThatTestRunnerLoadsTestsFromDifferentPlatforms() throws IOException {
        TestRunner runner = new TestRunner(Runnable::run, WORKING_DIR, LOGGER);
        assertDoesNotThrow(() -> runner.runTests(classLoader, TEST_SOURCES));

        File resultsFile = new File(WORKING_DIR, TestRunner.TEST_RESULTS_FILENAME);
        assertTrue(resultsFile.exists(), "Results file should have been created");

        try (FileReader reader = new FileReader(resultsFile)) {
            Map<String, TestResult.SuccessfulTestResult> results = GSON.fromJson(
                    reader,
                    new TypeToken<Map<String, TestResult.SuccessfulTestResult>>() {
                    }.getType()
            );

            List<Class<?>> testClasses = ResourceUtils.loadClasses(
                    classLoader,
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

                assertEquals(
                        0,
                        result.getFailedContainers(),
                        String.format("There should have been no failed containers: %s (%s)", result, testClassName)
                );
                assertEquals(
                        0,
                        result.getSkippedContainers(),
                        String.format("There should have been no skipped containers: %s (%s)", result, testClassName)
                );

                assertEquals(
                        0,
                        result.getFailedTests(),
                        String.format("There should have been no failed tests: %s (%s)", result, testClassName)
                );
                assertEquals(
                        0,
                        result.getSkippedTests(),
                        String.format("There should have been no skipped tests: %s (%s)", result, testClassName)
                );

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

}
