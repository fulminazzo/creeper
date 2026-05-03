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
 * Implementation of [PluginProvider] that will download plugins from their Modrinth page.
 *
 * @property downloader the downloader to use for downloading the plugins
 * @constructor Creates a new Modrinth plugin provider
 *
 * @param logger the logger to use for logging
 * @param executor the executor to use for asynchronous operations
 */
class ModrinthPluginProvider internal constructor(
    logger: Logger,
    executor: Executor,
    private val downloader: CachedDownloader
) : PluginProvider<ModrinthPluginRequest>(
    logger,
    executor
) {
    val cacheDuration = 6.hours

    private val cache = CacheManager[CACHE_FILE, VersionFile::class.java]

    /**
     * Attempts to get the requested version information from the API.
     *
     * @param request the request
     * @return the version information (or `null` if the version was not found)
     * @throws it.fulminazzo.creeper.util.HttpUtils.ApiException if the API returns an error
     */
    internal fun fetchVersionFileMetadata(request: ModrinthPluginRequest): CompletableFuture<VersionFile?> =
        cache[request.toHashString()]?.let { CompletableFuture.completedFuture(it) }
            ?: fetchVersionsMetadata(request.projectName).thenApply { r ->
                r?.let { raw -> JSON_MAPPER.readValue<List<Version>>(raw) }
                    ?.firstOrNull { it.versionNumber == request.version || it.name == request.version }
                    ?.files
                    ?.map { it.toVersionFile() }
                    ?.firstOrNull { it.name == request.name }
                    ?.let { versionFile ->
                        cache.set(request.toHashString(), versionFile, cacheDuration)
                        versionFile
                    }
            }

    /**
     * Attempts to get the raw versions information from the API.
     * If it fails, it attempts to fetch the slug of the given project name.
     * Then, it re-tries getting the raw versions with the new slug.
     *
     * @param projectName the project name
     * @return the versions or `null` if not found
     */
    private fun fetchVersionsMetadata(projectName: String): CompletableFuture<String?> {
        return HttpUtils.getApi(getVersionsUrl(projectName), executor).thenCompose { raw ->
            if (raw != null) CompletableFuture.completedFuture(raw)
            else HttpUtils.getApi(getSearchUrl(projectName), executor).thenApply { r ->
                r?.let { raw -> JSON_MAPPER.readValue<SearchResponse>(raw) }?.hits?.first()?.slug
            }.thenCompose { slug ->
                slug?.let { HttpUtils.getApi(getVersionsUrl(slug), executor) }
            }
        }
    }

    override fun handleRequest(directory: Path, request: ModrinthPluginRequest): CompletableFuture<Path> {
        logger.info("Fetching Modrinth release information for ${request.projectName} (version = ${request.version}, filename = ${request.name})")
        return fetchVersionFileMetadata(request).thenCompose { versionFile ->
            versionFile?.let {
                logger.info("Downloading plugin from ${versionFile.url}")
                downloader.download(versionFile.url, directory.resolve(versionFile.name), versionFile.hash)
            } ?: throw PluginNotFoundException(
                "Could not find Modrinth release for ${request.projectName} (version = ${request.version}, filename = ${request.name})"
            )
        }
    }

    internal companion object {
        internal val CACHE_FILE = CreeperPlugin.CACHE_DIRECTORY.resolve("modrinth.json")

        private val JSON_MAPPER = jacksonObjectMapper()

        /**
         * Gets the URL to get all the version information for a given project from the Modrinth API.
         *
         * @param projectName the name of the project in Modrinth
         * @return the URL
         */
        private fun getVersionsUrl(projectName: String): String =
            "https://api.modrinth.com/v2/project/${projectName.urlEncode()}/version"

        /**
         * Gets the URL to search for a project in the Modrinth API.
         *
         * @param projectName the name of the project to search for
         * @return the URL
         */
        private fun getSearchUrl(projectName: String): String {
            val search = "\"title:${projectName}\"".urlEncode()
            return "https://api.modrinth.com/v2/search?facets=[[$search]]"
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Version(
        val name: String,
        @JsonProperty("version_number")
        val versionNumber: String,
        val files: List<VersionFileResponse>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class VersionFileResponse(
        private val hashes: Hashes,
        private val filename: String,
        private val url: String
    ) {

        fun toVersionFile() = VersionFile(hashes.sha512, filename, url)

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Hashes(val sha512: String)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class SearchResponse(val hits: List<SearchHit>)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class SearchHit(val slug: String)

}

/**
 * Plugin request for [ModrinthPluginProvider].
 *
 * @property projectName the name of the project in Modrinth (either title or slug)
 * @property version the name or number of the version
 * @property name the name of the plugin file
 * @constructor Creates a new Modrinth plugin request
 */
data class ModrinthPluginRequest(
    val projectName: String,
    val version: String,
    val name: String
) : PluginRequest, Hashable {

    override fun toHashString(): String = "$projectName:$version:$name".sha256()

}

/**
 * Identifies a file in the Modrinth API response for a certain version.
 *
 * @property hash the SHA-512 hash of the file
 * @property name the name of the file
 * @constructor Creates a new Version file
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class VersionFile(val hash: String, val name: String, val url: String)