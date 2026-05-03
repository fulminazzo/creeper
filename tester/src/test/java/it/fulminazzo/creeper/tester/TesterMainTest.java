package it.fulminazzo.creeper.tester;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TesterMainTest {

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