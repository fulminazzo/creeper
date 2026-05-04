package it.fulminazzo.creeper.provider.plugin

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.CreeperPlugin.Companion.JSON_MAPPER
import it.fulminazzo.creeper.Hashable
import it.fulminazzo.creeper.cache.CacheManager
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.util.HttpUtils
import it.fulminazzo.creeper.util.sha256
import it.fulminazzo.creeper.util.urlEncode
import org.gradle.api.logging.Logger
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.hours

/**
 * Implementation of [PluginProvider] that will download plugins from their GitHub repositories.
 *
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new GitHub plugin provider
 *
 * @param logger the logger to use for logging
 */
class GitHubPluginProvider internal constructor(
    logger: Logger,
    private val downloader: CachedDownloader
) : PluginProvider<GitHubPluginRequest>(logger) {
    val cacheDuration = 6.hours

    private val cache = CacheManager[CACHE_FILE, Release::class.java]
    private val requestCache = ConcurrentHashMap<String, Optional<Release>>()

    /**
     * Attempts to get the requested release information from the API.
     *
     * @param request the request
     * @return the release information (or `null` if the release was not found)
     * @throws it.fulminazzo.creeper.util.HttpUtils.ApiException if the API returns an error
     */
    internal fun fetchReleaseMetadata(request: GitHubPluginRequest): Release? =
        cache[request.toHashString()] ?: requestCache.computeIfAbsent(request.toHashString()) {
            val raw = HttpUtils.getApi(getReleaseUrl(request.owner, request.repository, request.release))
                ?: return@computeIfAbsent Optional.empty()
            val release = JSON_MAPPER.readValue<ReleaseResponse>(raw).assets.firstOrNull { it.name == request.filename }
                ?: return@computeIfAbsent Optional.empty()
            cache.set(request.toHashString(), release, cacheDuration)
            Optional.of(release)
        }.orElse(null)

    override fun handleRequest(directory: Path, request: GitHubPluginRequest): Path {
        logger.lifecycle("Fetching GitHub release information for ${request.owner}/${request.repository}/${request.release} (filename =${request.filename})")
        return fetchReleaseMetadata(request)?.let { release ->
            logger.lifecycle("Downloading plugin from ${release.url}")
            downloader.download(release.url, directory.resolve(release.name), release.digest)
        } ?: throw PluginNotFoundException(
            "Could not find GitHub release for ${request.owner}/${request.repository}/${request.release} (filename = ${request.filename})"
        )
    }

    internal companion object {
        internal val CACHE_FILE = CreeperPlugin.CACHE_DIRECTORY.resolve("github.json")

        /**
         * Gets the URL to get release information from the GitHub API.
         *
         * @param owner the owner of the repository
         * @param repository the name of the repository
         * @param release the tag of the release
         * @return the URL
         */
        private fun getReleaseUrl(owner: String, repository: String, release: String): String =
            "https://api.github.com/repos/${owner.urlEncode()}/${repository.urlEncode()}/releases/tags/${release.urlEncode()}"

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ReleaseResponse(val assets: List<Release>)

}

/**
 * Plugin request for [GitHubPluginProvider].
 *
 * @property owner the owner of the repositoryhttps://modrinth.com/plugin/veinminer-enchantment
 * @property repository the name of the repository
 * @property release the tag of the release in the repository
 * @property filename the name of the plugin file
 * @constructor Creates a new GitHub plugin request
 */
data class GitHubPluginRequest(
    val owner: String,
    val repository: String,
    val release: String,
    val filename: String
) : PluginRequest, Hashable {

    override fun toHashString(): String = "$owner:$repository:$release:$filename".sha256()

}

/**
 * Identifies the GitHub API response for a certain release.
 *
 * @property url the URL to download the release from
 * @property name the name of the file
 * @property digest the SHA-256 digest of the release
 * @constructor Creates a new Release
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Release(
    @JsonProperty("browser_download_url")
    val url: String,
    val name: String,
    val digest: String
)