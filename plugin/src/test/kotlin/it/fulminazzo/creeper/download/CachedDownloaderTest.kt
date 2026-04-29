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
    private lateinit var delegate: Downloader

    private lateinit var downloader: CachedDownloader

    @BeforeEach
    fun setup() {
        val parent = DESTINATION_PATH.parent
        if (parent.exists()) parent.toFile().deleteRecursively()
        parent.createDirectories()

        delegate = mockk<Downloader>()
        every { delegate.download(RESOURCE_PATH, DESTINATION_PATH) } answers {
            arg<Path>(1).writeText("Hello, world!")
        }
        downloader = CachedDownloader.simple(delegate)
    }

    @Test
    fun `test that global downloader works with relative path`() {
        val cacheDirectory = CachedDownloader.CACHE_DIRECTORY
        cacheDirectory.toFile().deleteRecursively()

        val downloader = CachedDownloader.global(Downloader.http())
        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        checkFileExists()

        assertTrue(cacheDirectory.exists(), "Cache directory does not exist: ${cacheDirectory.toAbsolutePath()}")

        val cachedFile = cacheDirectory.resolve(DESTINATION_PATH)
        assertTrue(cachedFile.exists(), "Cached file does not exist: ${cachedFile.toAbsolutePath()}")

        val cachedChecksum = cacheDirectory.resolve(CHECKSUM_PATH)
        assertTrue(cachedChecksum.exists(), "Cached checksum does not exist: ${cachedChecksum.toAbsolutePath()}")
    }

    @Test
    fun `test that global downloader works with absolute path`() {
        val destinationPath = Path.of("/tmp/cached_downloader_test.txt")
        val checksumPath = destinationPath.resolveSibling("${destinationPath.name}.hash")

        val cacheDirectory = CachedDownloader.CACHE_DIRECTORY
        cacheDirectory.toFile().deleteRecursively()

        val downloader = CachedDownloader.global(Downloader.http())
        downloader.download(RESOURCE_PATH, destinationPath, HASH)
        assertTrue(destinationPath.exists(), "Destination file does not exist: ${destinationPath.toAbsolutePath()}")

        assertTrue(cacheDirectory.exists(), "Cache directory does not exist: ${cacheDirectory.toAbsolutePath()}")

        val cachedFile = cacheDirectory.resolve(destinationPath.root.relativize(destinationPath))
        assertTrue(cachedFile.exists(), "Cached file does not exist: ${cachedFile.toAbsolutePath()}")

        val cachedChecksum = cacheDirectory.resolve(checksumPath.root.relativize(checksumPath))
        assertTrue(cachedChecksum.exists(), "Cached checksum does not exist: ${cachedChecksum.toAbsolutePath()}")
    }

    @Test
    fun `test that downloader downloads resource only once`() {
        val downloader = CachedDownloader.simple(delegate)

        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        verify(exactly = 1) { delegate.download(RESOURCE_PATH, DESTINATION_PATH) }

        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        verify(exactly = 1) { delegate.download(RESOURCE_PATH, DESTINATION_PATH) }
    }

    @Test
    fun `test that downloader does not download resource if checksum matches`() {
        CHECKSUM_PATH.writeText(HASH)
        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        assertFalse(DESTINATION_PATH.exists())
    }

    @Test
    fun `test that downloader downloads resource if checksum does not match`() {
        CHECKSUM_PATH.writeText(HASH)
        downloader.download(RESOURCE_PATH, DESTINATION_PATH, "ABC")
        checkFileExists()
    }

    @Test
    fun `test that downloader downloads resource and stores checksum`() {
        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        checkFileExists()
        checkChecksumExists()
    }

    private fun checkFileExists() {
        assertTrue(DESTINATION_PATH.exists(), "Destination file does not exist: ${DESTINATION_PATH.toAbsolutePath()}")
    }

    private fun checkChecksumExists() {
        assertTrue(CHECKSUM_PATH.exists(), "Checksum file does not exist: ${CHECKSUM_PATH.toAbsolutePath()}")
    }

    companion object {
        private const val RESOURCE_PATH = "https://fulminazzo.it"
        private val DESTINATION_PATH = Path.of("build/resources/test/downloader/cached_downloader_test.txt")
        private val CHECKSUM_PATH = DESTINATION_PATH.resolveSibling("${DESTINATION_PATH.name}.hash")

        private const val HASH = "1234567890"

    }

}