package it.fulminazzo.creeper

import it.fulminazzo.creeper.CreeperPlugin.Companion.JSON_MAPPER
import it.fulminazzo.creeper.PlayerResolver.Companion.CACHE_FILE
import it.fulminazzo.creeper.cache.CacheManager
import it.fulminazzo.creeper.util.HttpUtils
import org.slf4j.Logger
import tools.jackson.module.kotlin.readValue
import java.util.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * A resolver for Minecraft players profiles.
 *
 * @property logger the logger to use to log messages
 * @constructor Creates a new Player resolver
 */
class PlayerResolver(private val logger: Logger) {
    private val cache = CacheManager[CACHE_FILE, UUID::class.java]

    /**
     * Gets the profiles of the requested players.
     * If [online] is true, the profiles will be fetched from the API.
     * Otherwise, they will be computed according to Bukkit rules.
     *
     * @param usernames the usernames of the players
     * @param online if the profiles should be fetched from the API
     * @return the profiles of the players
     */
    fun getPlayerProfiles(usernames: Collection<String>, online: Boolean): Set<PlayerProfile> =
        if (online) getOnlinePlayerProfiles(usernames)
        else usernames.map {
            PlayerProfile(
                UUID.nameUUIDFromBytes("$OFFLINE_PLAYER_PREFIX$it".toByteArray()),
                it
            )
        }.toSet()

    /**
     * Gets the profiles of the requested players from the API.
     * They will be cached under [CACHE_FILE] for future use.
     *
     * @param usernames the usernames of the players
     * @return the profiles of the players
     */
    fun getOnlinePlayerProfiles(usernames: Collection<String>): Set<PlayerProfile> {
        val uuids = mutableSetOf<PlayerProfile>()
        val missing = mutableListOf<String>()
        usernames.forEach { username ->
            cache[username]
                ?.let { uuids += PlayerProfile(it, username) }
                ?: missing.add(username)
        }
        if (missing.isNotEmpty()) {
            logger.info("Fetching the API for player ids of: ${missing.joinToString(", ")}")
            missing.chunked(MAXIMUM_PLAYERS).forEach { chunk ->
                val profiles = HttpUtils.postApi(API_URL, JSON_MAPPER.writeValueAsString(chunk))
                    ?.let { JSON_MAPPER.readValue<List<PlayerProfileResponse>>(it) }
                profiles?.let {
                    it.forEach { profile ->
                        val id = Uuid.parse(profile.id).toJavaUuid()
                        cache[profile.name] = id
                        uuids += PlayerProfile(id, profile.name)
                        missing.remove(profile.name)
                    }
                }
            }
            if (missing.isNotEmpty())
                logger.warn("Could not find player ids for: ${missing.joinToString(", ")}")
        }
        return uuids
    }

    internal companion object {
        private const val API_URL = "https://api.mojang.com/profiles/minecraft"

        internal val CACHE_FILE = CreeperPlugin.CACHE_DIRECTORY.resolve("mojang.json")

        /**
         * Maximum players per request allowed.
         * See https://minecraft.wiki/w/Mojang_API#Query_player_UUIDs_in_batch for more information.
         */
        private const val MAXIMUM_PLAYERS = 10

        private const val OFFLINE_PLAYER_PREFIX = "OfflinePlayer:"

    }

    /**
     * Identifies a player profile in an API response.
     *
     * @property id the UUID of the player
     * @property name the name of the player
     * @constructor Creates a new Player profile
     */
    private data class PlayerProfileResponse(val id: String, val name: String)

}

/**
 * Identifies a player profile.
 *
 * @property id the UUID of the player
 * @property name the name of the player
 * @constructor Creates a new Player profile
 */
data class PlayerProfile(val id: UUID, val name: String)