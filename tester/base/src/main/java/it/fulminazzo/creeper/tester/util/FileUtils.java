package it.fulminazzo.creeper.tester.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Collection of utilities to work with files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    /**
     * Finds all the compiled sources of a given source set, regardless of the JVM language.
     *
     * @param buildDirectory the {@code build} directory
     * @param sourceSetName the source set name
     * @return the list of compiled sources
     */
    public static @NotNull List<File> findCompiledSources(final @NotNull File buildDirectory,
                                                          final @NotNull String sourceSetName) {
        List<File> sources = new ArrayList<>();
        File classes = new File(buildDirectory, "classes");
        if (classes.isDirectory()) {
            File[] files = classes.listFiles();
            if (files != null)
                for (File file : files) {
                    File source = new File(file, sourceSetName);
                    if (source.isDirectory()) sources.add(source);
                }
        }
        return sources;
    }

}
