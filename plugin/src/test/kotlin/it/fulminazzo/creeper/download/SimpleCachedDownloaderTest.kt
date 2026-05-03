package it.fulminazzo.creeper.download

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleCachedDownloaderTest {
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
    fun `test that downloader downloads resource only once`() {
        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        verify(exactly = 1) { delegate.download(RESOURCE_PATH, DESTINATION_PATH) }

        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        verify(exactly = 1) { delegate.download(RESOURCE_PATH, DESTINATION_PATH) }
    }

    @Test
    fun `test that downloader does not download resource if checksum matches`() {
        CHECKSUM_PATH.writeText(HASH)
        val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        assertFalse(DESTINATION_PATH.exists())
        checkDestinationPath(path)
    }

    @Test
    fun `test that downloader downloads resource if checksum does not match`() {
        CHECKSUM_PATH.writeText(HASH)
        val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, "ABC")
        checkFileExists()
        checkDestinationPath(path)
    }

    @Test
    fun `test that downloader downloads resource and stores checksum`() {
        val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        checkFileExists()
        checkChecksumExists()
        checkDestinationPath(path)
    }

    private fun checkDestinationPath(path: Path) {
        assertEquals(DESTINATION_PATH, path, "Destination path did not match")
    }

    private fun checkFileExists() {
        assertTrue(DESTINATION_PATH.exists(), "Destination file does not exist: ${DESTINATION_PATH.toAbsolutePath()}")
    }

    private fun checkChecksumExists() {
        assertTrue(CHECKSUM_PATH.exists(), "Checksum file does not exist: ${CHECKSUM_PATH.toAbsolutePath()}")
    }

    companion object {
        private const val RESOURCE_PATH = "https://fulminazzo.it"
        private val DESTINATION_PATH = Path.of("build/resources/test/download/cached_downloader_test.txt")
        private val CHECKSUM_PATH = DESTINATION_PATH.resolveSibling("${DESTINATION_PATH.name}.hash")

        private const val HASH = "1234567890"

    }

}