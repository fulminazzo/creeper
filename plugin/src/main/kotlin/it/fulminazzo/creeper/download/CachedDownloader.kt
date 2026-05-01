package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.download.CachedDownloader.Companion.HASH_EXTENSION
import it.fulminazzo.creeper.download.CachedDownloader.GlobalCachedDownloader.Companion.hashUrl
import it.fulminazzo.creeper.util.sha256
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
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
    fun download(resource: String, destination: Path, hash: String): CompletableFuture<Path>

    companion object {
        private const val HASH_EXTENSION = "hash"

        /**
         * Creates a special [CachedDownloader] that stores the requested resource in a global local cache directory.
         * Then, it copies the cached file to the requested destination.
         *
         * @param delegate the downloader used to download the files
         * @return the cached downloader
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
     * The files are stored with the following rule:
     * if the current path is "/home/fulminazzo" and the resource path is "https://www.fulminazzo.it/plugins/Main-1.0.jar",
     * then the file will be cached under "/home/fulminazzo/.gradle/caches/[ProjectInfo.NAME]/<hash_of_website>/plugins/Main-1.0.jar"
     * where the hash will be computed with [hashUrl].
     *
     * @constructor Creates a new Global Cached downloader
     *
     * @param downloader the downloader used to download the files
     */
    class GlobalCachedDownloader internal constructor(downloader: Downloader) : CachedDownloader {
        private val delegate = SimpleCachedDownloader(downloader)
        private val operations = ConcurrentHashMap<String, CompletableFuture<Path>>()

        override fun download(resource: String, destination: Path, hash: String): CompletableFuture<Path> {
            return operations.computeIfAbsent(hashUrl(resource)) { u ->
                val cacheDestination = CreeperPlugin.CACHE_DIRECTORY.resolve(u)
                delegate.download(resource, cacheDestination, hash)
            }.thenApply { downloadedFile ->
                destination.parent.createDirectories()
                downloadedFile.copyTo(destination, overwrite = true)
                destination
            }
        }

        internal companion object {

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
                finalUrl = finalUrl.sha256() + uri.path.replace("/", File.separator)
                if (query != null) finalUrl += "?$query"
                return finalUrl
            }

        }

    }

    /**
     * Base implementation of [CachedDownloader] that delegates the download part to a [Downloader].
     *
     * @property delegate the downloader used to download the files
     * @constructor Creates a new Cached downloader
     */
    private class SimpleCachedDownloader(private val delegate: Downloader) : CachedDownloader {

        override fun download(resource: String, destination: Path, hash: String): CompletableFuture<Path> {
            return CompletableFuture.supplyAsync {
                val checksum = destination.resolveSibling("${destination.name}.$HASH_EXTENSION")
                if (!checksum.exists() || hash != checksum.readText()) {
                    delegate.download(resource, destination)
                    checksum.writeText(hash)
                }
                destination
            }
        }

    }

}