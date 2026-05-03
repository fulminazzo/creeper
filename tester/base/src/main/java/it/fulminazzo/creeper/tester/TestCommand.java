package it.fulminazzo.creeper.tester;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Command to execute the tests.
 */
@RequiredArgsConstructor
public final class TestCommand {
    private final @NotNull TesterApplication application;
    private final @NotNull Consumer<String> messageSender;

    /**
     * Executes the tests.
     */
    public void execute() {
        messageSender.accept("Preparing tests execution.");
        messageSender.accept("WARNING: to ensure maximum compatibility, the tests will be run synchronously.");
        messageSender.accept("Be prepared for lag spikes and server halts.");
        new TestsRunner(application.dataDirectory(), application.logger()).runTests(TestCommand.class.getClassLoader());
        messageSender.accept(String.format(
                "Tests execution completed. Check the application directory for the results. (%s)",
                application.dataDirectory().getPath()
        ));
    }

}
