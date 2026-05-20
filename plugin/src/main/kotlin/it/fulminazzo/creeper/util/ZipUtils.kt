package it.fulminazzo.creeper.util

import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream

/**
 * A collection of utilities for working with zip files.
 */
object ZipUtils {

    /**
     * Unzips the given zip file to the specified destination.
     *
     * @param zipFile the zip file to unzip
     * @param destination the destination directory
     */
    fun unzip(zipFile: Path, destination: Path) {
        destination.createDirectories()

        val input = ZipInputStream(zipFile.inputStream())
        var entry = input.nextEntry
        while (entry != null) {
            val target = destination.resolve(entry.name)
            if (entry.isDirectory) target.createDirectories()
            else {
                target.parent.createDirectories()
                target.toFile().outputStream().use { output -> input.copyTo(output) }
            }
            entry = input.nextEntry
        }
    }

}