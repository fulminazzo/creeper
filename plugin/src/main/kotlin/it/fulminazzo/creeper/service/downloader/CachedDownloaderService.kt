package it.fulminazzo.creeper.service.downloader

import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Service for the global [CachedDownloader].
 */
abstract class CachedDownloaderService : BuildService<CachedDownloaderService.Params> {

    val downloader: CachedDownloader by lazy {
        CachedDownloader.global(parameters.downloader.get().downloader)
    }

    interface Params : BuildServiceParameters {

        val downloader: Property<DownloaderService>

    }

}