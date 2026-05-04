package it.fulminazzo.creeper.tester;

import it.fulminazzo.creeper.tester.util.FileUtils;
import it.fulminazzo.creeper.tester.util.ResourceUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
        try {
            messageSender.accept("Preparing tests execution.");
            messageSender.accept("WARNING: to ensure maximum compatibility, the tests will be run synchronously.");
            messageSender.accept("Be prepared for lag spikes and server halts.");

            ClassLoader classLoader = TestCommand.class.getClassLoader();
            File buildDirectory = new File(ResourceUtils.readResource(classLoader, "METADATA.creeper"));
            @NotNull List<File> mainSources = FileUtils.findCompiledSources(buildDirectory, "main");
            @NotNull List<File> integrationTestSources = FileUtils.findCompiledSources(buildDirectory, "integrationTest");
            String testsPackage = FileUtils.findMainPackage(integrationTestSources);

            try (URLClassLoader tmpClassLoader = new URLClassLoader(
                    Stream.concat(mainSources.stream(), integrationTestSources.stream()).map(File::toURI).map(f -> {
                        try {
                            return f.toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }).distinct().toArray(URL[]::new),
                    classLoader
            )) {
                new TestsRunner(
                        testsPackage,
                        application.dataDirectory(),
                        application.logger()
                ).runTests(tmpClassLoader);
            }

            messageSender.accept(String.format(
                    "Tests execution completed. Check the application directory for the results. (%s)",
                    application.dataDirectory().getPath()
            ));
        } catch (Exception e) {
            messageSender.accept("Error while running tests: " + e.getMessage());
            application.logger().warn("Error while running tests: {}", e.getMessage(), e);
        }
    }

}
