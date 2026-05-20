package it.fulminazzo.creeper.service.downloader

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Service for a simple HTTP [Downloader].
 */
abstract class DownloaderService : BuildService<BuildServiceParameters.None> {

    val downloader: Downloader by lazy {
        Downloader.http()
    }

}