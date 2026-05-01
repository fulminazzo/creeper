package it.fulminazzo.creeper.download

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.fulminazzo.creeper.CreeperPlugin
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GlobalCachedDownloaderIntegrationTest {
    private val downloader = CachedDownloader.global(Downloader.http())
    private val cacheDirectory = CreeperPlugin.CACHE_DIRECTORY

    @BeforeEach
    fun setup() {
        cacheDirectory.toFile().deleteRecursively()
    }

    @Test
    fun `test that global downloader caches requests and only downloads once`() {
        val delegate = mockk<Downloader>()
        every { delegate.download(any(), any()) } answers {
            val destination = arg<Path>(1)
            destination.parent.createDirectories()
            destination.writeText("Hello, world!")
            Thread.sleep(1_000)
        }

        val downloader = CachedDownloader.global(delegate)

        val resource = "https://www.google.com"
        val path1 = Path.of("build/resources/test/download/global_cached_downloader_test1.txt")
        val path2 = Path.of("build/resources/test/download/global_cached_downloader_test2.txt")
        val hash = "1234567890"

        val first = downloader.download(resource, path1, hash).join()
        val second = downloader.download(resource, path2, hash).join()

        assertNotEquals(first, second, "Returned paths should not be equal")
        assertEquals(first, path1, "First path should be $path1")
        assertEquals(second, path2, "Second path should be $path2")
        verify(exactly = 1) { delegate.download(resource, any()) }
    }

    @Test
    fun `test that global downloader works with relative path`() {
        val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH).join()
        assertTrue(
            DESTINATION_PATH.exists(),
            "Destination file does not exist: ${DESTINATION_PATH.toAbsolutePath()}"
        )
        assertEquals(DESTINATION_PATH, path, "Destination and downloaded path did not match")

        checkCacheFiles(cacheDirectory)
    }

    @Test
    fun `test that global downloader works with absolute path`() {
        val destinationPath = Path.of("/tmp/cached_downloader_test.txt")

        val path = downloader.download(RESOURCE_PATH, destinationPath, HASH).join()
        assertTrue(destinationPath.exists(), "Destination file does not exist: ${destinationPath.toAbsolutePath()}")
        assertEquals(destinationPath, path, "Destination and downloaded path did not match")

        checkCacheFiles(cacheDirectory)
    }

    private fun checkCacheFiles(cacheDirectory: Path) {
        assertTrue(cacheDirectory.exists(), "Cache directory does not exist: ${cacheDirectory.toAbsolutePath()}")

        val fileName = CachedDownloader.GlobalCachedDownloader.hashUrl(RESOURCE_PATH)
        val cachedFile = cacheDirectory.resolve(fileName)
        assertTrue(cachedFile.exists(), "Cached file does not exist: ${cachedFile.toAbsolutePath()}")

        val cachedChecksum = cacheDirectory.resolve("$fileName.hash")
        assertTrue(cachedChecksum.exists(), "Cached checksum does not exist: ${cachedChecksum.toAbsolutePath()}")
    }

    companion object {
        private const val RESOURCE_PATH = "https://fulminazzo.it"
        private val DESTINATION_PATH = Path.of("build/resources/integrationTest/download/cached_downloader_test.txt")

        private const val HASH = "1234567890"

    }

}
