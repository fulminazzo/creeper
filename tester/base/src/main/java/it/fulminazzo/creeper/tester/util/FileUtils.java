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
     * Finds the main package assuming the given sources are compiled classes to their root packages.
     *
     * @param sources the sources to analyze
     * @return the main package
     */
    public static @NotNull String findMainPackage(final @NotNull List<File> sources) {
        List<File> subpackages = new ArrayList<>();
        String base = null;
        for (File source : sources) {
            if (!source.isDirectory()) return "";
            File[] files = source.listFiles();
            if (files != null)
                for (File file : files) {
                    if (base == null) base = file.getName();
                    if (!file.getName().equals(base) || !file.isDirectory()) return "";
                    subpackages.add(file);
                }
        }
        if (base == null) return "";
        String subpackage = findMainPackage(subpackages);
        return subpackage.isEmpty() ? base : base + "." + subpackage;
    }

    /**
     * Finds all the compiled sources of a given source set, regardless of the JVM language.
     *
     * @param buildDirectory the {@code build} directory
     * @param sourceSetName  the source set name
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
