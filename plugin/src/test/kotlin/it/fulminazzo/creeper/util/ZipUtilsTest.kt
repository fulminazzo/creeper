package it.fulminazzo.creeper.util

import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import kotlin.test.Test

class ZipUtilsTest {

    @Test
    fun `test that unzip works`() {
        val destination = File("build/resources/test/zip_test/archive").absoluteFile
        if (destination.exists()) destination.deleteRecursively()

        val zipFile = File("build/resources/test/archive.zip")

        ZipUtils.unzip(zipFile.toPath(), destination.toPath())

        listOf(
            destination,
            File(destination, "file1.txt"),
            File(destination, "dir"),
            File(destination, "dir${File.separatorChar}file2.txt"),
            File(destination, "dir${File.separatorChar}nested"),
            File(destination, "dir${File.separatorChar}nested${File.separatorChar}file3.txt")
        ).forEach { file ->
            assertTrue(
                file.exists(),
                "File ${file.absolutePath} does not exist"
            )
        }
    }

}