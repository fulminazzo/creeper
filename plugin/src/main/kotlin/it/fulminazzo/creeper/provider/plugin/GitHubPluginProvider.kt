package it.fulminazzo.creeper.provider.plugin

import it.fulminazzo.creeper.Hashable
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.util.sha256
import org.slf4j.Logger
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.hours

/**
 * Implementation of [PluginProvider] that will download plugins from their GitHub repositories.
 *
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new GitHub plugin provider
 *
 * @param directory the directory to download plugins to
 * @param logger the logger to use for logging
 */
class GitHubPluginProvider(
    directory: Path,
    logger: Logger,
    private val downloader: CachedDownloader
) : PluginProvider<GitHubPluginRequest>(
    directory,
    logger
) {
    val cacheDuration = 6.hours

    override fun handleRequest(request: GitHubPluginRequest): CompletableFuture<Path> {
        TODO("Not yet implemented")
    }

    /**
     * Gets the cached release for the given request from the global cache.
     * Checks [cacheDuration] to see if the release is not expired.
     *
     * @param request the request
     * @return the cached release (if found)
     */
    internal fun getTimedCachedRelease(request: GitHubPluginRequest): Release? =
        getCachedRelease(request)
            ?.takeIf { it.updated + cacheDuration.inWholeMilliseconds >= System.currentTimeMillis() }
            ?.release

    internal companion object {
        internal val CACHE_FILE = CachedDownloader.CACHE_DIRECTORY.resolve("github.json")
        private val CACHE: MutableMap<String, ReleaseCache> by lazy {
            if (CACHE_FILE.exists())
                JSON_MAPPER.readValue<ConcurrentHashMap<String, ReleaseCache>>(CACHE_FILE.toFile())
            else ConcurrentHashMap()
        }

        private val JSON_MAPPER = jacksonObjectMapper()

        /**
         * Gets the cached release for the given request from the global cache.
         *
         * @param request the request
         * @return the cached release (if found)
         */
        internal fun getCachedRelease(request: GitHubPluginRequest): ReleaseCache? = CACHE[request.toHashString()]

        /**
         * Updates the cache of the given request with the fetched release.
         *
         * @param request the request
         * @param release the fetched release
         */
        internal fun updateCache(request: GitHubPluginRequest, release: Release) {
            CACHE[request.toHashString()] = ReleaseCache(release, System.currentTimeMillis())
            CACHE_FILE.toFile().writeText(JSON_MAPPER.writeValueAsString(CACHE))
        }

    }

}

/**
 * Plugin request for [GitHubPluginProvider].
 *
 * @property owner the owner of the repository
 * @property repository the name of the repository
 * @property release the tag of the release in the repository
 * @property name the name of the plugin file
 * @constructor Creates a new GitHub plugin request
 */
data class GitHubPluginRequest(
    val owner: String,
    val repository: String,
    val release: String,
    val name: String
) : PluginRequest, Hashable {

    override fun toHashString(): String = "$owner:$repository:$release:$name".sha256()

}

/**
 * Identifies the GitHub API response for a certain release.
 *
 * @property url the URL to download the release from
 * @property digest the SHA-256 digest of the release
 * @constructor Creates a new Release
 */
internal data class Release(val url: String, val digest: String)

/**
 * Identifies a cache for the GitHub API response for a certain release.
 *
 * @property release the release
 * @property updated the time the cache was updated
 * @constructor Creates a new Release cache
 */
internal data class ReleaseCache(val release: Release, val updated: Long)