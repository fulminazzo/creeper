package it.fulminazzo.creeper.provider.plugin

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.Hashable
import it.fulminazzo.creeper.cache.CacheManager
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.util.HttpUtils
import it.fulminazzo.creeper.util.sha256
import it.fulminazzo.creeper.util.urlEncode
import org.slf4j.Logger
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.time.Duration.Companion.hours

/**
 * Implementation of [PluginProvider] that will download plugins from their GitHub repositories.
 *
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new GitHub plugin provider
 *
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 */
class GitHubPluginProvider internal constructor(
    logger: Logger,
    executor: Executor,
    private val downloader: CachedDownloader
) : PluginProvider<GitHubPluginRequest>(
    logger,
    executor
) {
    val cacheDuration = 6.hours

    private val cache = CacheManager[CACHE_FILE, Release::class.java]

    /**
     * Attempts to get the requested release information from the API.
     *
     * @param request the request
     * @return the release information (or `null` if the release was not found)
     * @throws it.fulminazzo.creeper.util.HttpUtils.ApiException if the API returns an error
     */
    internal fun fetchReleaseMetadata(request: GitHubPluginRequest): CompletableFuture<Release?> =
        cache[request.toHashString()]?.let { CompletableFuture.completedFuture(it) }
            ?: HttpUtils.getApi(getReleaseUrl(request.owner, request.repository, request.release), executor)
                .thenApply { r ->
                    r?.let { raw -> JSON_MAPPER.readValue<ReleaseResponse>(raw) }
                        ?.assets
                        ?.firstOrNull { it.name == request.name }
                        ?.let { release ->
                            cache.set(request.toHashString(), release, cacheDuration)
                            release
                        }
                }

    override fun handleRequest(directory: Path, request: GitHubPluginRequest): CompletableFuture<Path> {
        logger.info("Fetching GitHub release information for ${request.owner}/${request.repository}/${request.release} (filename =${request.name})")
        return fetchReleaseMetadata(request).thenCompose { release ->
            release?.let {
                logger.info("Downloading plugin from ${release.url}")
                downloader.download(release.url, directory.resolve(release.name), release.digest)
            } ?: throw PluginNotFoundException(
                "Could not find GitHub release for ${request.owner}/${request.repository}/${request.release} (filename = ${request.name})"
            )
        }
    }

    internal companion object {
        internal val CACHE_FILE = CreeperPlugin.CACHE_DIRECTORY.resolve("github.json")

        private val JSON_MAPPER = jacksonObjectMapper()

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