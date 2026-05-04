package it.fulminazzo.creeper.service.provider.plugin

import it.fulminazzo.creeper.provider.plugin.PluginProvider
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.provider.plugin.RedirectPluginProvider
import it.fulminazzo.creeper.service.downloader.CachedDownloaderService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Service for a generic [PluginProvider] supporting all types of requests.
 */
abstract class PluginProviderService : BuildService<PluginProviderService.Params> {

    private val logger = Logging.getLogger(PluginProviderService::class.java)

    val provider: PluginProvider<PluginRequest> by lazy {
        RedirectPluginProvider(
            logger,
            parameters.downloader.get().downloader,
        )
    }

    interface Params : BuildServiceParameters {

        val downloader: Property<CachedDownloaderService>

    }

}