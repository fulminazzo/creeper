package it.fulminazzo.creeper.tester;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class TesterMainTest {
    private static final @NotNull ClassLoader CLASS_LOADER = TesterMainTest.class.getClassLoader();

    private static final @NotNull File WORKING_DIR = new File("build/resources/test/tester_main");
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TesterMainTest.class);

    private static final @NotNull Gson GSON = new Gson();

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

                TesterMain.ExceptionData data = result.getException();
                assertEquals(
                        RuntimeException.class.getCanonicalName(),
                        data.getExceptionName(),
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

        TesterMain.ExceptionData expected = new TesterMain.ExceptionData(
                RuntimeException.class.getCanonicalName(),
                "test",
                generateStacktrace(main),
                new TesterMain.ExceptionData(
                        IllegalArgumentException.class.getCanonicalName(),
                        "cause",
                        generateStacktrace(cause),
                        new TesterMain.ExceptionData(
                                IllegalStateException.class.getCanonicalName(),
                                null,
                                generateStacktrace(subcause),
                                null
                        )
                )
        );

        TesterMain.ExceptionData actual = TesterMain.ExceptionData.of(main);

        assertEquals(expected, actual, "ExceptionData should be generated correctly");
    }

    private static @NotNull List<String> generateStacktrace(final @NotNull Exception exception) {
        return Arrays.stream(exception.getStackTrace()).map(Object::toString).collect(Collectors.toList());
    }

}