package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.ProjectInfo
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

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
     */
    fun download(resource: String, destination: Path, hash: String)

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
         */
        fun global(delegate: Downloader): CachedDownloader = GlobalCachedDownloader(delegate)

        /**
         * Creates a new [CachedDownloader] that delegates the download part to a [Downloader].
         *
         * @param delegate the downloader used to download the files
         * @return the cached downloader
         */
        fun simple(delegate: Downloader): CachedDownloader = SimpleCachedDownloader(delegate)

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
     */
    private class GlobalCachedDownloader(downloader: Downloader) : CachedDownloader {
        private val delegate = SimpleCachedDownloader(downloader)

        override fun download(resource: String, destination: Path, hash: String) {
            val current = Path.of("").absolute().normalize()
            val absolute = destination.absolute().normalize()

            val relative = if (absolute.startsWith(current))
                current.relativize(absolute)
            else absolute.root.relativize(absolute)

            val cacheDestination = CACHE_DIRECTORY.resolve(relative)
            delegate.download(resource, cacheDestination, hash)
            cacheDestination.copyTo(destination, overwrite = true)
        }

    }

    /**
     * Base implementation of [CachedDownloader] that delegates the download part to a [Downloader].
     *
     * @property delegate the downloader used to download the files
     * @constructor Creates a new Cached downloader
     */
    private class SimpleCachedDownloader(private val delegate: Downloader) : CachedDownloader {

        override fun download(resource: String, destination: Path, hash: String) {
            val checksum = destination.resolveSibling("${destination.name}.$HASH_EXTENSION")
            if (checksum.exists()) {
                val expectedChecksum = checksum.readText()
                if (hash == expectedChecksum) return
            }
            delegate.download(resource, destination)
            checksum.writeText(hash)
        }

    }

}