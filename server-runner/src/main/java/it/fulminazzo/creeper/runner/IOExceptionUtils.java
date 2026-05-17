package it.fulminazzo.creeper.runner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class IOExceptionUtils {
    private static final @NotNull List<String> INVALID_MESSAGES = Arrays.asList(
            "Socket closed",
            "Connection reset",
            "Broken pipe"
    );

    public static boolean isValidException(final @NotNull IOException e) {
        String message = e.getMessage();
        return message == null || INVALID_MESSAGES.stream().noneMatch(message::contains);
    }

}
