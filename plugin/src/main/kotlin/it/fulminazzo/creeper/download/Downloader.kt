package it.fulminazzo.creeper.download

import java.nio.file.Path

/**
 * Function to download resources from the web.
 */
fun interface Downloader {

    /**
     * Downloads the requested resource.
     *
     * @param resource the resource path on the web
     * @param destination the destination path where it will be stored
     */
    fun download(resource: String, destination: Path)

}