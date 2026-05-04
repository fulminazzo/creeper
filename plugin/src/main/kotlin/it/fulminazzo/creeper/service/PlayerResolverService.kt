package it.fulminazzo.creeper.service

import it.fulminazzo.creeper.PlayerResolver
import org.gradle.api.logging.Logging
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Service for [PlayerResolver].
 */
abstract class PlayerResolverService : BuildService<BuildServiceParameters.None> {

    private val logger = Logging.getLogger(PlayerResolverService::class.java)

    val playerResolver: PlayerResolver by lazy {
        PlayerResolver(logger)
    }

}