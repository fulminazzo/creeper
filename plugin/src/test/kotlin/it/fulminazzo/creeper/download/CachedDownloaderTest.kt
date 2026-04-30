package it.fulminazzo.creeper.download

import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

class CachedDownloaderTest {
    private lateinit var delegate: Downloader
    private val scope = CoroutineScope(Dispatchers.IO)

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
        downloader = CachedDownloader.simple(delegate, scope)
    }

    @Test
    fun `test that downloader downloads resource only once`() {
        runBlocking {
            downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH).await()
            verify(exactly = 1) { delegate.download(RESOURCE_PATH, DESTINATION_PATH) }

            downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH).await()
            verify(exactly = 1) { delegate.download(RESOURCE_PATH, DESTINATION_PATH) }
        }
    }

    @Test
    fun `test that downloader does not download resource if checksum matches`() {
        runBlocking {
            CHECKSUM_PATH.writeText(HASH)
            val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH).await()
            assertFalse(DESTINATION_PATH.exists())
            checkDestinationPath(path)
        }
    }

    @Test
    fun `test that downloader downloads resource if checksum does not match`() {
        runBlocking {
            CHECKSUM_PATH.writeText(HASH)
            val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, "ABC").await()
            checkFileExists()
            checkDestinationPath(path)
        }
    }

    @Test
    fun `test that downloader downloads resource and stores checksum`() {
        runBlocking {
            val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH).await()
            checkFileExists()
            checkChecksumExists()
            checkDestinationPath(path)
        }
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
        private val DESTINATION_PATH = Path.of("build/resources/test/downloader/cached_downloader_test.txt")
        private val CHECKSUM_PATH = DESTINATION_PATH.resolveSibling("${DESTINATION_PATH.name}.hash")

        private const val HASH = "1234567890"

    }

}