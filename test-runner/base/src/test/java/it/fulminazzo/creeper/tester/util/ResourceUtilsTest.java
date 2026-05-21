package it.fulminazzo.creeper.tester.util;

import it.fulminazzo.creeper.tester.util.classes.First;
import it.fulminazzo.creeper.tester.util.classes.Second;
import it.fulminazzo.creeper.tester.util.classes.subpackage.Third;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceUtilsTest {
    private static final @NotNull ClassLoader CLASS_LOADER = ResourceUtilsTest.class.getClassLoader();

    private static final @NotNull File WORKING_DIR = new File("build/resources/test/util/resource_utils");

    @Test
    void testThatLoadClassesWorks() throws IOException {
        List<Class<?>> classes = ResourceUtils.loadClasses(
                CLASS_LOADER,
                ResourceUtilsTest.class.getPackage().getName() + ".classes"
        );
        for (Class<?> expected : Arrays.asList(First.class, Second.class, Third.class))
            assertTrue(
                    classes.contains(expected),
                    String.format("Class %s not present in classes: %s", expected, classes)
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "util/resource_utils/first.txt",
            "util/resource_utils/second.txt",
            "util/resource_utils/directory/third.txt"
    })
    void testThatListResourcesCorrectlyFiltersResources(final @NotNull String resource) throws IOException {
        List<String> results = ResourceUtils.listResources(
                CLASS_LOADER,
                "",
                r -> r.contains(resource)
        );
        assertTrue(
                results.contains(resource),
                String.format("Resource %s not present in resources: %s", resource, results)
        );
    }

    @Test
    void testThatLoadFromJarWorks() throws IOException, URISyntaxException {
        Path jar = createTestJar();
        URL url = new URI(String.format("jar:%s!/", jar.toUri())).toURL();
        List<String> results = new ArrayList<>();
        ResourceUtils.loadFromJar(url, results, r -> !r.contains("schema"));
        assertTrue(
                results.contains("test.txt"),
                String.format("Resource 'test.txt' not present in resources: %s", results)
        );
    }

    @Test
    void testThatLoadFromFileSystemWorks() throws IOException {
        URL url = WORKING_DIR.toURI().toURL();
        List<String> results = new ArrayList<>();
        ResourceUtils.loadFromFileSystem(url, "", results, r -> !r.contains("second"));

        for (String resource : Arrays.asList("first.txt", "directory/third.txt"))
            assertTrue(
                    results.contains(resource),
                    String.format("Resource '%s' not present in resources: %s", resource, results)
            );
    }

    private Path createTestJar() throws IOException {
        Map<String, String> entries = new HashMap<>();
        entries.put("test.txt", "Hello, world");
        entries.put("data/schema.sql", "CREATE TABLE secret (id INT);");
        Path jar = Files.createTempFile("test-resources", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                String name = entry.getKey();
                String content = entry.getValue();
                output.putNextEntry(new JarEntry(name));
                output.write(content.getBytes(StandardCharsets.UTF_8));
                output.closeEntry();
            }
        }
        return jar;
    }

}