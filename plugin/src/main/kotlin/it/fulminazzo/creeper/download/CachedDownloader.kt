package it.fulminazzo.creeper.download

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * A downloader that caches the downloaded files in a local directory.
 *
 * @property downloader the downloader used to download the files
 * @constructor Creates a new Cached downloader
 */
class CachedDownloader(private val downloader: Downloader = Downloader.http()) {

    /**
     * If a file with name [destination].[HASH_EXTENSION] exists and matches
     * the provided hash, the resource will not be downloaded.
     * Otherwise, a new download will be initialized and a checksum file will be created.
     *
     * @param resource the resource path on the web
     * @param destination the destination path where it will be stored
     * @param hash the hash of the resource (to compare with the local one)
     */
    fun download(resource: String, destination: Path, hash: String) {
        val checksum = destination.resolveSibling("${destination.name}.$HASH_EXTENSION")
        if (checksum.exists()) {
            val expectedChecksum = checksum.readText()
            if (hash == expectedChecksum) return
        }
        downloader.download(resource, destination)
        checksum.writeText(hash)
    }

    companion object {
        private const val HASH_EXTENSION = "hash"

    }

}