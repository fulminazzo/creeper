package it.fulminazzo.creeper.tester.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * A collection of utilities to work with resources.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceUtils {

    /**
     * Loads all the classes of the given package that match the filter.
     *
     * @param classLoader the classloader to check into
     * @param packageName the package name
     * @return the loaded classes
     * @throws IOException if an error occurs while getting the classes
     */
    public static @NotNull List<Class<?>> loadClasses(final @NotNull ClassLoader classLoader,
                                                      final @NotNull String packageName) throws IOException {
        return listClassNames(classLoader, packageName).stream()
                .map(c -> {
                    try {
                        return classLoader.loadClass(c);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("Unreachable code", e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Lists all the class names of the given package that match the filter.
     *
     * @param classLoader the classloader to check into
     * @param packageName the package name
     * @return the list of class names
     * @throws IOException if an error occurs while listing the classes
     */
    public static @NotNull List<String> listClassNames(final @NotNull ClassLoader classLoader,
                                                       final @NotNull String packageName) throws IOException {
        final String extension = ".class";
        return listResources(classLoader, packageName.replace(".", "/"), r -> r.endsWith(extension)).stream()
                .map(r -> r.substring(0, r.length() - extension.length()).replace("/", "."))
                .collect(Collectors.toList());
    }

    /**
     * Lists all the resources of the given classloader that match the filter in the specified folder.
     *
     * @param classLoader     the classloader
     * @param resourcesFolder the resources folder (should NOT have a preceding "/")
     * @param filter          the filter to apply to the resources
     * @return the list of resources
     * @throws IOException if an error occurs while listing the resources
     */
    public static @NotNull List<String> listResources(final @NotNull ClassLoader classLoader,
                                                      final @NotNull String resourcesFolder,
                                                      final @NotNull Predicate<String> filter) throws IOException {
        final List<String> results = new ArrayList<>();
        final Enumeration<URL> urls = classLoader.getResources(resourcesFolder);
        while (urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            if (url.getProtocol().equals("jar")) loadFromJar(url, results, filter);
            else loadFromFileSystem(url, resourcesFolder, results, filter);
        }
        return results;
    }

    /**
     * Loads all the resources of the given JAR URL that match the filter.
     *
     * @param url     the JAR URL
     * @param results the list to add the results to
     * @param filter  the filter to apply to the resources
     * @throws IOException if an error occurs while loading the resources
     */
    static void loadFromJar(final @NotNull URL url,
                            final @NotNull List<String> results,
                            final @NotNull Predicate<String> filter) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        connection.setUseCaches(false);
        String entryPrefix = connection.getEntryName();
        try (JarFile jarFile = connection.getJarFile()) {
            jarFile.stream()
                    .filter(e -> !e.isDirectory())
                    .map(ZipEntry::getName)
                    .filter(n -> entryPrefix == null || n.startsWith(entryPrefix))
                    .filter(filter)
                    .forEach(results::add);
        }
    }

    /**
     * Loads all the files from the given URL that match the filter.
     *
     * @param url             the URL to load from
     * @param resourcesFolder the folder to prepend to every file path
     * @param results         the list to add the results to
     * @param filter          the filter to apply to the files
     * @throws IOException if an error occurs while loading the files
     */
    static void loadFromFileSystem(final @NotNull URL url,
                                   final @NotNull String resourcesFolder,
                                   final @NotNull List<String> results,
                                   final @NotNull Predicate<String> filter) throws IOException {
        final char separator = '/';
        final Path directory = Paths.get(url.getPath());
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .map(directory::relativize)
                    .map(p -> (resourcesFolder.isEmpty() ? "" : resourcesFolder + separator) +
                            p.toString().replace(File.separatorChar, separator)
                    )
                    .filter(filter)
                    .forEach(results::add);
        }
    }

}