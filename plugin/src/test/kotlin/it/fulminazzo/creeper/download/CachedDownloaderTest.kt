package it.fulminazzo.creeper.download

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.test.Test

class CachedDownloaderTest {
    private val resourcePath = "https://raw.githubusercontent.com/gradle/gradle/master/gradle.properties"
    private val destinationPath = Path.of("build/resources/test/downloader/http_downloader_test.txt")
    private val checksumPath = destinationPath.resolveSibling("${destinationPath.name}.hash")

    private val hash = "1234567890"

    private val downloader = CachedDownloader(Downloader.http())

    @BeforeEach
    fun setup() {
        val parent = destinationPath.parent
        if (parent.exists()) parent.toFile().deleteRecursively()
        parent.createDirectories()
    }

    @Test
    fun `test that downloader downloads resource only once`() {
        val mockDownloader = mockk<Downloader>()
        val downloader = CachedDownloader(mockDownloader)
        every { mockDownloader.download(resourcePath, destinationPath) } just Runs

        downloader.download(resourcePath, destinationPath, hash)
        verify(exactly = 1) { mockDownloader.download(resourcePath, destinationPath) }

        downloader.download(resourcePath, destinationPath, hash)
        verify(exactly = 1) { mockDownloader.download(resourcePath, destinationPath) }
    }

    @Test
    fun `test that downloader does not download resource if checksum matches`() {
        checksumPath.writeText(hash)
        downloader.download(resourcePath, destinationPath, hash)
        assertFalse(destinationPath.exists())
    }

    @Test
    fun `test that downloader downloads resource if checksum does not match`() {
        checksumPath.writeText(hash)
        downloader.download(resourcePath, destinationPath, "ABC")
        assertTrue(destinationPath.exists())
    }

    @Test
    fun `test that downloader downloads resource and stores checksum`() {
        downloader.download(resourcePath, destinationPath, hash)
        assertTrue(destinationPath.exists())
        assertTrue(checksumPath.exists())
    }

}