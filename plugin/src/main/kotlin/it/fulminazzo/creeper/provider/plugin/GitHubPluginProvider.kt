package it.fulminazzo.creeper.provider.plugin

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.Hashable
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.util.HttpUtils
import it.fulminazzo.creeper.util.sha256
import org.slf4j.Logger
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.math.log
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
class GitHubPluginProvider internal constructor(
    directory: Path,
    logger: Logger,
    private val downloader: CachedDownloader
) : PluginProvider<GitHubPluginRequest>(
    directory,
    logger
) {
    val cacheDuration = 6.hours

    /**
     * Attempts to get the requested release information from the API.
     *
     * @param request the request
     * @return the release information (or `null` if the release was not found)
     * @throws it.fulminazzo.creeper.util.HttpUtils.ApiException if the API returns an error
     */
    internal fun fetchReleaseMetadata(request: GitHubPluginRequest): CompletableFuture<Release?> =
        getTimedCachedRelease(request)?.let { CompletableFuture.completedFuture(it) }
            ?: HttpUtils.getApi(getReleaseUrl(request.owner, request.repository, request.release)).thenApply { r ->
                r?.let { raw -> JSON_MAPPER.readValue<ReleaseResponse>(raw) }
                    ?.assets
                    ?.firstOrNull { it.name == request.name }
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

    override fun handleRequest(request: GitHubPluginRequest): CompletableFuture<Path> {
        logger.info("Fetching release information for ${request.owner}/${request.repository}/${request.release} (name = ${request.name})")
        return fetchReleaseMetadata(request).thenCompose { release ->
            release?.let {
                logger.info("Downloading plugin from ${release.url}")
                downloader.download(release.url, directory.resolve(release.name), release.digest)
            } ?: throw ReleaseNotFoundException(request)
        }
    }

    internal companion object {
        internal val CACHE_FILE = CreeperPlugin.CACHE_DIRECTORY.resolve("github.json")
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
            CACHE_FILE.parent.createDirectories()
            CACHE_FILE.toFile().writeText(JSON_MAPPER.writeValueAsString(CACHE))
        }

        /**
         * Gets the URL to get release information from the GitHub API.
         *
         * @param owner the owner of the repository
         * @param repository the name of the repository
         * @param release the tag of the release
         * @return the URL
         */
        private fun getReleaseUrl(owner: String, repository: String, release: String): String =
            "https://api.github.com/repos/$owner/$repository/releases/tags/$release"

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ReleaseResponse(val assets: List<Release>)

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
 * @property name the name of the file
 * @property digest the SHA-256 digest of the release
 * @constructor Creates a new Release
 */
internal data class Release(
    @JsonProperty("browser_download_url")
    val url: String,
    val name: String,
    val digest: String
)

/**
 * Identifies a cache for the GitHub API response for a certain release.
 *
 * @property release the release
 * @property updated the time the cache was updated
 * @constructor Creates a new Release cache
 */
internal data class ReleaseCache(val release: Release, val updated: Long)

/**
 * Exception thrown when a release could not be found.
 *
 * @constructor Create a new Release not found exception
 *
 * @param request the request that was made
 */
class ReleaseNotFoundException internal constructor(request: GitHubPluginRequest) :
    Exception("Could not find release for ${request.owner}/${request.repository}/${request.release} (name = ${request.name})")