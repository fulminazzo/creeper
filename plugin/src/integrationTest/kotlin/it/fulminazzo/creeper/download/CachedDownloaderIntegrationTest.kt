package it.fulminazzo.creeper.download

import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.test.Test

class CachedDownloaderIntegrationTest {

    @Test
    fun `test that global downloader works with relative path`() {
        val cacheDirectory = CachedDownloader.CACHE_DIRECTORY
        cacheDirectory.toFile().deleteRecursively()

        val downloader = CachedDownloader.global(Downloader.http())
        downloader.download(RESOURCE_PATH, DESTINATION_PATH, HASH)
        assertTrue(DESTINATION_PATH.exists(), "Destination file does not exist: ${DESTINATION_PATH.toAbsolutePath()}")

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

    companion object {
        private const val RESOURCE_PATH = "https://fulminazzo.it"
        private val DESTINATION_PATH = Path.of("build/resources/integrationTest/downloader/cached_downloader_test.txt")
        private val CHECKSUM_PATH = DESTINATION_PATH.resolveSibling("${DESTINATION_PATH.name}.hash")

        private const val HASH = "1234567890"

    }

}
