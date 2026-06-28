package it.fulminazzo.creeper.tester;

import it.fulminazzo.creeper.tester.util.FileUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Command to execute the tests.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class TestCommand {
    @NotNull TesterApplication application;
    @NotNull Consumer<String> messageSender;

    /**
     * Executes the tests.
     */
    public void execute() {
        try {
            File configurationFile = application.configuration();
            Map<String, Object> configuration = new HashMap<>();
            if (configurationFile.exists())
                try (FileReader fileReader = new FileReader(configurationFile)) {
                    configuration = new Yaml().loadAs(fileReader, Map.class);
                }

            final String buildDirectoryPath = Objects.requireNonNull(
                    configuration.get("build-directory-path"),
                    "Could not find 'build-directory-path' from configuration"
            ).toString();
            final List<String> dependencies = extractDependencies(configuration);

            ClassLoader classLoader = TestCommand.class.getClassLoader();
            File buildDirectory = new File(buildDirectoryPath).getAbsoluteFile();

            messageSender.accept(String.format("Preparing tests execution for directory: %s.", buildDirectory.getPath()));
            messageSender.accept(String.format("Using %s dependencies.", dependencies.size()));
            messageSender.accept("WARNING: to ensure maximum compatibility, the tests will be run synchronously.");
            messageSender.accept("Be prepared for lag spikes and server halts.");

            @NotNull List<File> mainSources = FileUtils.findCompiledSources(buildDirectory, "main");
            messageSender.accept("Found " + mainSources.size() + " main sources.");
            @NotNull List<File> functionalTestSources = FileUtils.findCompiledSources(buildDirectory, "functionalTest");
            messageSender.accept("Found " + functionalTestSources.size() + " functional test sources.");
            String testsPackage = FileUtils.findMainPackage(functionalTestSources);
            messageSender.accept("Tests package: " + testsPackage);

            try (URLClassLoader tmpClassLoader = new URLClassLoader(
                    Stream.concat(
                            Stream.concat(mainSources.stream(), functionalTestSources.stream()),
                            dependencies.stream().map(File::new)
                    ).map(File::toURI).map(f -> {
                        try {
                            return f.toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }).distinct().toArray(URL[]::new),
                    classLoader
            )) {
                messageSender.accept("Running tests...");
                new TestRunner(
                        application.testWorker(),
                        testsPackage,
                        application.dataDirectory(),
                        application.logger()
                ).runTests(tmpClassLoader, functionalTestSources);
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

    private static @NotNull List<String> extractDependencies(final @NotNull Map<String, Object> configuration) {
        Object rawDependencies = configuration.get("dependencies");
        if (rawDependencies instanceof Collection)
            return ((Collection<?>) rawDependencies).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        else return Collections.emptyList();
    }

}
