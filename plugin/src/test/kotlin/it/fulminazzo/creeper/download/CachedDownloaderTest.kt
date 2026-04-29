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
    private val resourcePath = "https://fulminazzo.it"
    private val destinationPath = Path.of("build/resources/test/downloader/cached_downloader_test.txt")
    private val checksumPath = destinationPath.resolveSibling("${destinationPath.name}.hash")

    private val hash = "1234567890"

    private lateinit var delegate: Downloader

    private lateinit var downloader: CachedDownloader

    @BeforeEach
    fun setup() {
        val parent = destinationPath.parent
        if (parent.exists()) parent.toFile().deleteRecursively()
        parent.createDirectories()

        delegate = mockk<Downloader>()
        every { delegate.download(resourcePath, destinationPath) } answers {
            arg<Path>(1).writeText("Hello, world!")
        }
        downloader = CachedDownloader.simple(delegate)
    }

    @Test
    fun `test that global downloader works`() {
        val cacheDirectory = CachedDownloader.CACHE_DIRECTORY
        cacheDirectory.toFile().deleteRecursively()

        val downloader = CachedDownloader.global(Downloader.http())
        downloader.download(resourcePath, destinationPath, hash)
        checkFileExists()

        assertTrue(cacheDirectory.exists(), "Cache directory does not exist: ${cacheDirectory.toAbsolutePath()}")

        val cachedFile = cacheDirectory.resolve(destinationPath)
        assertTrue(cachedFile.exists(), "Cached file does not exist: ${cachedFile.toAbsolutePath()}")

        val cachedChecksum = cacheDirectory.resolve(checksumPath).exists()
        assertTrue(cachedChecksum, "Cached checksum does not exist: ${checksumPath.toAbsolutePath()}")
    }

    @Test
    fun `test that downloader downloads resource only once`() {
        val downloader = CachedDownloader.simple(delegate)

        downloader.download(resourcePath, destinationPath, hash)
        verify(exactly = 1) { delegate.download(resourcePath, destinationPath) }

        downloader.download(resourcePath, destinationPath, hash)
        verify(exactly = 1) { delegate.download(resourcePath, destinationPath) }
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
        checkFileExists()
    }

    @Test
    fun `test that downloader downloads resource and stores checksum`() {
        downloader.download(resourcePath, destinationPath, hash)
        checkFileExists()
        checkChecksumExists()
    }

    private fun checkFileExists() {
        assertTrue(destinationPath.exists(), "Destination file does not exist: ${destinationPath.toAbsolutePath()}")
    }

    private fun checkChecksumExists() {
        assertTrue(checksumPath.exists(), "Checksum file does not exist: ${checksumPath.toAbsolutePath()}")
    }

}