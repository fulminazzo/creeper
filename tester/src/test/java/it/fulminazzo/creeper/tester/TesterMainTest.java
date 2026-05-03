package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TesterMainTest {
    private static final @NotNull String TEST_EXECUTION_SUMMARY_TYPE = "org.junit.platform.launcher.listeners.MutableTestExecutionSummary";

    private static final @NotNull ClassLoader CLASS_LOADER = TesterMainTest.class.getClassLoader();

    private static final @NotNull File WORKING_DIR = new File("build/resources/test/tester_main");
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TesterMainTest.class);

    private static final @NotNull Gson GSON = new Gson();

    @Test
    void testThatRunTestsCorrectlyReportsTestsSummary() throws ClassNotFoundException, IOException, NoSuchFieldException {
        try (MockedStatic<LauncherFactory> mock = mockStatic(LauncherFactory.class)) {
            TesterMain.SuccessfulTestsResult expected = new TesterMain.SuccessfulTestsResult(
                    1000L,
                    2000L,
                    1L,
                    3L,
                    2L,
                    6L,
                    Collections.singletonList(
                            new TesterMain.Failure(
                                    "Container failure",
                                    "Container failed execution!",
                                    new TesterMain.TestSource(
                                            TesterMainTest.class.getCanonicalName(),
                                            null,
                                            null
                                    ),
                                    new TesterMain.ThrowableData(
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
                            new TesterMain.Failure(
                                    "First test failure",
                                    "Test method failed execution!",
                                    new TesterMain.TestSource(
                                            TesterMainTest.class.getCanonicalName(),
                                            "testThatRunTestsCorrectlyReportsTestsSummary",
                                            "''"
                                    ),
                                    new TesterMain.ThrowableData(
                                            RuntimeException.class.getCanonicalName(),
                                            "Test method failed execution!",
                                            Collections.emptyList(),
                                            null
                                    )
                            ),
                            new TesterMain.Failure(
                                    "Second test failure",
                                    "Test class failed execution!",
                                    null,
                                    new TesterMain.ThrowableData(
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
                    when(source.getClassName()).thenReturn(TesterMainTest.class.getCanonicalName());
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
                    when(source.getClassName()).thenReturn(TesterMainTest.class.getCanonicalName());
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

            mock.when(LauncherFactory::create).thenReturn(launcher);

            TesterMain main = new TesterMain(WORKING_DIR, LOGGER);
            assertDoesNotThrow(() -> main.runTests(CLASS_LOADER));

            File resultsFile = new File(WORKING_DIR, TesterMain.TEST_RESULTS_FILENAME);
            assertTrue(resultsFile.exists(), "Results file should have been created");

            try (FileReader reader = new FileReader(resultsFile)) {
                TesterMain.SuccessfulTestsResult result = GSON.fromJson(reader, TesterMain.SuccessfulTestsResult.class);
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
        try (MockedStatic<Files> mock = mockStatic(Files.class)) {
            mock.when(() -> Files.createDirectories(any())).thenAnswer(a -> {
                Path path = a.getArgument(0);
                path.toFile().mkdirs();
                throw new RuntimeException("Test exception");
            });

            TesterMain main = new TesterMain(WORKING_DIR, LOGGER);
            assertDoesNotThrow(() -> main.runTests(CLASS_LOADER));

            File resultsFile = new File(WORKING_DIR, TesterMain.TEST_RESULTS_FILENAME);
            assertTrue(resultsFile.exists(), "Results file should have been created");

            try (FileReader reader = new FileReader(resultsFile)) {
                TesterMain.ExceptionResult result = GSON.fromJson(reader, TesterMain.ExceptionResult.class);
                assertFalse(result.isSuccess(), "Test should have failed");

                TesterMain.ThrowableData data = result.getException();
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
        TesterMain main = new TesterMain(new File("/tests/"), LOGGER);
        assertDoesNotThrow(() -> main.runTests(CLASS_LOADER));
    }

    @Test
    void testThatExceptionDataGeneratesCorrectData() {
        Exception subcause = new IllegalStateException();
        Exception cause = new IllegalArgumentException("cause", subcause);
        Exception main = new RuntimeException("test", cause);

        TesterMain.ThrowableData expected = new TesterMain.ThrowableData(
                RuntimeException.class.getCanonicalName(),
                "test",
                generateStacktrace(main),
                new TesterMain.ThrowableData(
                        IllegalArgumentException.class.getCanonicalName(),
                        "cause",
                        generateStacktrace(cause),
                        new TesterMain.ThrowableData(
                                IllegalStateException.class.getCanonicalName(),
                                null,
                                generateStacktrace(subcause),
                                null
                        )
                )
        );

        TesterMain.ThrowableData actual = TesterMain.ThrowableData.of(main);

        assertEquals(expected, actual, "ExceptionData should be generated correctly");
    }

    private static @NotNull List<String> generateStacktrace(final @NotNull Exception exception) {
        return Arrays.stream(exception.getStackTrace()).map(Object::toString).collect(Collectors.toList());
    }

}