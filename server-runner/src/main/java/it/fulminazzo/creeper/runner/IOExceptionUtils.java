package it.fulminazzo.creeper.runner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class IOExceptionUtils {

    public static boolean isValidException(final @NotNull IOException e) {
        String message = e.getMessage();
        return message == null || !message.equals("Socket closed");
    }

}
