package it.fulminazzo.creeper.service.provider

import it.fulminazzo.creeper.provider.ConfigProvider
import it.fulminazzo.creeper.provider.MCJarsApiProvider
import it.fulminazzo.creeper.service.downloader.CachedDownloaderService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Service for [ConfigProvider].
 */
abstract class ConfigProviderService : BuildService<ConfigProviderService.Params> {

    private val logger = Logging.getLogger(ConfigProviderService::class.java)

    val configProvider: ConfigProvider by lazy {
        MCJarsApiProvider(
            parameters.downloader.get().downloader,
            logger
        )
    }

    interface Params : BuildServiceParameters {

        val downloader: Property<CachedDownloaderService>

    }

}