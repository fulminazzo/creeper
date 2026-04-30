package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.download.CachedDownloader.Companion.HASH_EXTENSION
import it.fulminazzo.creeper.util.sha256
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.*

/**
 * A downloader that caches the downloaded files in a local directory.
 */
interface CachedDownloader {

    /**
     * If a file with name [destination].[HASH_EXTENSION] exists and matches
     * the provided hash, the resource will not be downloaded.
     * Otherwise, a new download will be initialized and a checksum file will be created.
     *
     * @param resource the resource path on the web
     * @param destination the destination path where it will be stored
     * @param hash the hash of the resource (to compare with the local one)
     * @return the path of the newly downloaded file
     */
    fun download(resource: String, destination: Path, hash: String): Deferred<Path>

    companion object {
        /**
         * The global cache directory.
         */
        internal val CACHE_DIRECTORY
            get() = Path.of(System.getProperty("user.home"), ".gradle", "caches", ProjectInfo.NAME)

        private const val HASH_EXTENSION = "hash"

        /**
         * Creates a special [CachedDownloader] that stores the requested resource in a global local cache directory.
         * Then, it copies the cached file to the requested destination.
         *
         * @param delegate the downloader used to download the files
         * @param scope the scope of the coroutine
         * @return the cached downloader
         */
        fun global(delegate: Downloader, scope: CoroutineScope): CachedDownloader =
            GlobalCachedDownloader(delegate, scope)

        /**
         * Creates a new [CachedDownloader] that delegates the download part to a [Downloader].
         *
         * @param delegate the downloader used to download the files
         * @param scope the scope of the coroutine
         * @return the cached downloader
         */
        fun simple(delegate: Downloader, scope: CoroutineScope): CachedDownloader =
            SimpleCachedDownloader(delegate, scope)

    }

    /**
     * Special [CachedDownloader] that stores the requested resource globally
     * before storing it in the actual destination.
     *
     * The files are stored with the following rules:
     * - If the current path is "/home/fulminazzo" and the destination path is "plugins/Main-1.0.jar",
     *   then the file will be cached under "/home/fulminazzo/.gradle/caches/[ProjectInfo.NAME]/plugins/Main-1.0.jar";
     * - If the current path is "/home/fulminazzo" and the destination path is "/home/fulminazzo/plugins/Main-1.0.jar",
     *   then the file will be cached under "/home/fulminazzo/.gradle/caches/[ProjectInfo.NAME]/plugins/Main-1.0.jar";
     * - If the current path is "/home/fulminazzo" and the destination path is "/server/plugins/Main-1.0.jar",
     *   then the file will be cached under "/home/fulminazzo/.gradle/caches/[ProjectInfo.NAME]/server/plugins/Main-1.0.jar".
     *
     * @constructor Creates a new Global Cached downloader
     *
     * @param downloader the downloader used to download the files
     * @property scope the scope of the coroutine
     */
    class GlobalCachedDownloader(downloader: Downloader, private val scope: CoroutineScope) : CachedDownloader {
        private val delegate = SimpleCachedDownloader(downloader, scope)

        override fun download(resource: String, destination: Path, hash: String): Deferred<Path> {
            return scope.async {
                val url = hashUrl(resource)
                val cacheDestination = CACHE_DIRECTORY.resolve(url)
                val downloadedFile = delegate.download(resource, cacheDestination, hash).await()
                destination.parent.createDirectories()
                downloadedFile.copyTo(destination, overwrite = true)
                destination
            }
        }

        companion object {

            /**
             * Extracts a hash digest of the given URL.
             *
             * @param url the URL to hash
             * @return the hash digest
             */
            fun hashUrl(url: String): String {
                val uri = URI(url).normalize()
                val scheme = uri.scheme
                var port = uri.port
                if (port == 80 && scheme == "http") port = -1
                else if (port == 443 && scheme == "https") port = -1
                val query = uri.rawQuery
                var finalUrl = "$scheme://${uri.host}"
                if (port != -1) finalUrl += ":$port"
                finalUrl += uri.path
                if (query != null) finalUrl += "?$query"
                return finalUrl.sha256()
            }

        }

    }

    /**
     * Base implementation of [CachedDownloader] that delegates the download part to a [Downloader].
     *
     * @property delegate the downloader used to download the files
     * @property scope the scope of the coroutine
     * @constructor Creates a new Cached downloader
     */
    private class SimpleCachedDownloader(
        private val delegate: Downloader,
        private val scope: CoroutineScope
    ) : CachedDownloader {

        override fun download(resource: String, destination: Path, hash: String): Deferred<Path> {
            return scope.async {
                val checksum = destination.resolveSibling("${destination.name}.$HASH_EXTENSION")
                if (checksum.exists()) {
                    val expectedChecksum = checksum.readText()
                    if (hash == expectedChecksum) return@async destination
                }
                delegate.download(resource, destination)
                checksum.writeText(hash)
                destination
            }
        }

    }

}