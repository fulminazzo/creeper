package it.fulminazzo.creeper.download

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCachedDownloaderIntegrationTest {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val downloader = CachedDownloader.global(Downloader.http(), scope)

    @Test
    fun `test that global downloader works with relative path`() {
        runBlocking {
            val cacheDirectory = CachedDownloader.CACHE_DIRECTORY
            cacheDirectory.toFile().deleteRecursively()

            val path = downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH).await()
            assertTrue(
                DESTINATION_PATH.exists(),
                "Destination file does not exist: ${DESTINATION_PATH.toAbsolutePath()}"
            )
            assertEquals(DESTINATION_PATH, path, "Destination and downloaded path did not match")

            checkCacheFiles(cacheDirectory)
        }
    }

    @Test
    fun `test that global downloader works with absolute path`() {
        runBlocking {
            val destinationPath = Path.of("/tmp/cached_downloader_test.txt")

            val cacheDirectory = CachedDownloader.CACHE_DIRECTORY
            cacheDirectory.toFile().deleteRecursively()

            val path = downloader.download(RESOURCE_PATH, destinationPath, HASH).await()
            assertTrue(destinationPath.exists(), "Destination file does not exist: ${destinationPath.toAbsolutePath()}")
            assertEquals(destinationPath, path, "Destination and downloaded path did not match")

            checkCacheFiles(cacheDirectory)
        }
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
        private val DESTINATION_PATH = Path.of("build/resources/integrationTest/downloader/cached_downloader_test.txt")

        private const val HASH = "1234567890"

    }

}
