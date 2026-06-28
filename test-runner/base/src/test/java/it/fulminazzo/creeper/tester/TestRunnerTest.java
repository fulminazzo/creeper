package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestRunnerTest {
    private static final @NotNull String TEST_EXECUTION_SUMMARY_TYPE = "org.junit.platform.launcher.listeners.MutableTestExecutionSummary";

    private static final @NotNull ClassLoader CLASS_LOADER = TestRunnerTest.class.getClassLoader();

    private static final @NotNull File WORKING_DIR = new File("build/resources/test/tester_main");
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TestRunnerTest.class);

    private static final @NotNull Gson GSON = new Gson();

    private static final @NotNull TestWorker WORKER = Runnable::run;

    @Test
    void testThatRunTestsCorrectlyReportsTestsSummary() throws ClassNotFoundException, IOException, NoSuchFieldException {
        try (
                MockedStatic<LauncherFactory> factoryMock = mockStatic(LauncherFactory.class);
                MockedStatic<TestRunner> testRunnerMock = mockStatic(TestRunner.class)
        ) {
            TestResult.SuccessfulTestResult expected = new TestResult.SuccessfulTestResult(
                    1000L,
                    2000L,
                    1L,
                    3L,
                    2L,
                    6L,
                    Collections.singletonList(
                            new TestResult.Failure(
                                    "Container failure",
                                    "Container failed execution!",
                                    new TestResult.TestSource(
                                            TestRunnerTest.class.getCanonicalName(),
                                            null,
                                            null
                                    ),
                                    new TestResult.ThrowableData(
                                            Error.class.getCanonicalName(),
                                            "Container failed execution!",
                                            Collections.emptyList(),
                                            null
                                    )
                            )
                    ),
                    2L,
                    3L,
                    2L,
                    7L,
                    Arrays.asList(
                            new TestResult.Failure(
                                    "First test failure",
                                    "Test method failed execution!",
                                    new TestResult.TestSource(
                                            TestRunnerTest.class.getCanonicalName(),
                                            "testThatRunTestsCorrectlyReportsTestsSummary",
                                            "''"
                                    ),
                                    new TestResult.ThrowableData(
                                            RuntimeException.class.getCanonicalName(),
                                            "Test method failed execution!",
                                            Collections.emptyList(),
                                            null
                                    )
                            ),
                            new TestResult.Failure(
                                    "Second test failure",
                                    "Test class failed execution!",
                                    null,
                                    new TestResult.ThrowableData(
                                            Exception.class.getCanonicalName(),
                                            "Test class failed execution!",
                                            Collections.emptyList(),
                                            null
                                    )
                            )
                    )
            );

            final List<TestExecutionSummary.Failure> failures = new ArrayList<>();

            TestExecutionSummary summary = (TestExecutionSummary) mock(Class.forName(TEST_EXECUTION_SUMMARY_TYPE));
            when(summary.getTimeStarted()).thenReturn(1000L);
            when(summary.getTimeFinished()).thenReturn(2000L);
            when(summary.getFailures()).thenReturn(failures);

            // Containers
            when(summary.getContainersFailedCount()).thenReturn(1L);
            when(summary.getContainersSucceededCount()).thenReturn(3L);
            when(summary.getContainersSkippedCount()).thenReturn(2L);
            when(summary.getContainersFoundCount()).thenReturn(6L);

            TestExecutionSummary.Failure containerFailure = mock(TestExecutionSummary.Failure.class);
            when(containerFailure.getTestIdentifier()).thenAnswer(a -> {
                TestIdentifier identifier = mock(TestIdentifier.class);
                when(identifier.isContainer()).thenReturn(true);
                when(identifier.isTest()).thenReturn(false);
                when(identifier.getDisplayName()).thenReturn("Container failure");
                when(identifier.getSource()).thenAnswer(a2 -> {
                    ClassSource source = mock(ClassSource.class);
                    when(source.getClassName()).thenReturn(TestRunnerTest.class.getCanonicalName());
                    return Optional.of(source);
                });
                return identifier;
            });
            when(containerFailure.getException()).thenReturn(new Error("Container failed execution!"));
            failures.add(containerFailure);

            // Tests
            when(summary.getTestsFailedCount()).thenReturn(2L);
            when(summary.getTestsSucceededCount()).thenReturn(3L);
            when(summary.getTestsSkippedCount()).thenReturn(2L);
            when(summary.getTestsFoundCount()).thenReturn(7L);

            TestExecutionSummary.Failure firstTestFailure = mock(TestExecutionSummary.Failure.class);
            when(firstTestFailure.getTestIdentifier()).thenAnswer(a -> {
                TestIdentifier identifier = mock(TestIdentifier.class);
                when(identifier.isContainer()).thenReturn(false);
                when(identifier.isTest()).thenReturn(true);
                when(identifier.getDisplayName()).thenReturn("First test failure");
                when(identifier.getSource()).thenAnswer(a2 -> {
                    MethodSource source = mock(MethodSource.class);
                    when(source.getClassName()).thenReturn(TestRunnerTest.class.getCanonicalName());
                    when(source.getMethodName()).thenReturn("testThatRunTestsCorrectlyReportsTestsSummary");
                    when(source.getMethodParameterTypes()).thenReturn("''");
                    return Optional.of(source);
                });
                return identifier;
            });
            when(firstTestFailure.getException()).thenReturn(new RuntimeException("Test method failed execution!"));
            failures.add(firstTestFailure);

            TestExecutionSummary.Failure secondTestFailure = mock(TestExecutionSummary.Failure.class);
            when(secondTestFailure.getTestIdentifier()).thenAnswer(a -> {
                TestIdentifier identifier = mock(TestIdentifier.class);
                when(identifier.isContainer()).thenReturn(false);
                when(identifier.isTest()).thenReturn(true);
                when(identifier.getDisplayName()).thenReturn("Second test failure");
                when(identifier.getSource()).thenReturn(Optional.ofNullable(mock(TestSource.class)));
                return identifier;
            });
            when(secondTestFailure.getException()).thenReturn(new Exception("Test class failed execution!"));
            failures.add(secondTestFailure);

            Field field = SummaryGeneratingListener.class.getDeclaredField("summary");
            field.setAccessible(true);
            List<SummaryGeneratingListener> listeners = new ArrayList<>();
            Launcher launcher = mock(Launcher.class);
            doAnswer(a -> {
                TestExecutionListener listener = a.getArgument(0);
                if (listener instanceof SummaryGeneratingListener) listeners.add((SummaryGeneratingListener) listener);
                return null;
            }).when(launcher).registerTestExecutionListeners(any());
            doAnswer(a -> {
                listeners.forEach(l -> {
                    try {
                        field.set(l, summary);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
                return null;
            }).when(launcher).execute(any(LauncherDiscoveryRequest.class));

            factoryMock.when(LauncherFactory::create).thenReturn(launcher);

            testRunnerMock.when(() -> TestRunner.extractTestClasses(any())).thenReturn(List.of(TestRunnerTest.class.getCanonicalName()));

            TestRunner runner = new TestRunner(WORKER, WORKING_DIR, LOGGER);
            assertDoesNotThrow(() -> runner.runTests(CLASS_LOADER, new ArrayList<>()));

            File resultsFile = new File(WORKING_DIR, TestRunner.TEST_RESULTS_FILENAME);
            assertTrue(resultsFile.exists(), "Results file should have been created");

            try (FileReader reader = new FileReader(resultsFile)) {
                Map<String, TestResult.SuccessfulTestResult> results = GSON.fromJson(
                        reader,
                        new TypeToken<Map<String, TestResult.SuccessfulTestResult>>() {
                        }.getType()
                );

                TestResult.SuccessfulTestResult result = results.get(TestRunnerTest.class.getCanonicalName());
                assertNotNull(result, "Test result should not be null");
                assertTrue(result.isSuccess(), "Test should have not failed");

                // Remove stacktrace to compare with expected
                result.getContainerFailures().forEach(f -> f.getException().setStackTrace(Collections.emptyList()));
                result.getTestFailures().forEach(f -> f.getException().setStackTrace(Collections.emptyList()));

                assertEquals(expected, result, "Results did not match expected result");
            }
        }
    }

    @Test
    void testThatRunTestsDoesNotThrowOnExceptionDuringExecutionAndCorrectlyStoresResults() throws IOException {
        try (
                MockedStatic<LauncherFactory> factoryMock = mockStatic(LauncherFactory.class);
                MockedStatic<TestRunner> testRunnerMock = mockStatic(TestRunner.class)
        ) {
            AtomicBoolean exceptionThrown = new AtomicBoolean(false);
            factoryMock.when(LauncherFactory::create).thenAnswer(_ -> {
                if (!exceptionThrown.get()) {
                    exceptionThrown.set(true);
                    throw new RuntimeException("Test exception");
                }
                return mock(Launcher.class);
            });

            testRunnerMock.when(() -> TestRunner.extractTestClasses(any())).thenReturn(List.of(TestRunnerTest.class.getCanonicalName()));

            TestRunner runner = new TestRunner(WORKER, WORKING_DIR, LOGGER);
            assertDoesNotThrow(() -> runner.runTests(CLASS_LOADER, new ArrayList<>()));

            File resultsFile = new File(WORKING_DIR, TestRunner.TEST_RESULTS_FILENAME);
            assertTrue(resultsFile.exists(), "Results file should have been created");

            try (FileReader reader = new FileReader(resultsFile)) {
                Map<String, TestResult.ThrowableResult> results = GSON.fromJson(
                        reader,
                        new TypeToken<Map<String, TestResult.ThrowableResult>>() {
                        }.getType()
                );

                TestResult.ThrowableResult result = results.get(TestRunnerTest.class.getCanonicalName());
                assertNotNull(result, "Test result should not be null");
                assertFalse(result.isSuccess(), "Test should have failed");

                TestResult.ThrowableData data = result.getException();
                assertEquals(
                        RuntimeException.class.getCanonicalName(),
                        data.getThrowableName(),
                        "Exception name did not match exception class name"
                );
                assertEquals(
                        "Test exception",
                        data.getMessage(),
                        "Exception message did not match exception message"
                );
                assertNotNull(data.getStackTrace(), "Exception stacktrace should not be null");
                assertFalse(data.getStackTrace().isEmpty(), "Exception stacktrace should not be empty");
                assertNull(data.getCause(), "Exception cause should be null");
            }
        }
    }

    @Test
    void testThatRunTestsDoesNotThrowOnWriteException() {
        TestRunner runner = new TestRunner(WORKER, new File("/tests/"), LOGGER);
        assertDoesNotThrow(() -> runner.runTests(CLASS_LOADER, new ArrayList<>()));
    }

}