package it.fulminazzo.creeper.cache

import com.fasterxml.jackson.annotation.JsonIgnore
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.CreeperPlugin.Companion.JSON_MAPPER
import it.fulminazzo.creeper.ProjectInfo
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.Closeable
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.time.Duration

/**
 * A manager for handling cache of specific data types.
 * It stores the data in a JSON file and provides methods to get and put values.
 *
 * @param V the type of the value stored in the cache
 * @property file the file where the cache is stored
 * @property valueClass the class of the value stored in the cache
 * @constructor Creates a new Cache manager
 */
class CacheManager<V> private constructor(
    private val file: Path,
    private val valueClass: Class<V>
) : Closeable {
    private val javaType = JSON_MAPPER.typeFactory.constructMapType(
        ConcurrentHashMap::class.java,
        JSON_MAPPER.typeFactory.constructType(String::class.java),
        JSON_MAPPER.typeFactory.constructParametricType(TimedValue::class.java, valueClass)
    )
    private val cache: MutableMap<String, TimedValue<V>> by lazy {
        if (file.exists()) JSON_MAPPER.readValue(file.toFile(), javaType)
        else ConcurrentHashMap()
    }

    private val executor = Executors.newSingleThreadExecutor {
        Thread(it, "${ProjectInfo.NAME}-cache-${file.absolutePathString()}")
    }
    private var pendingWrite: Future<*>? = null

    /**
     * Gets the value associated with the specified key from the cache.
     * If the value is expired, it is removed from the cache and `null` is returned.
     *
     * @param key the key of the value to get
     * @return the value associated with the key, or `null` if the value is expired or not present
     */
    operator fun get(key: String): V? = cache[key]?.takeIf { !it.isExpired() }?.value

    /**
     * Stores the value in the cache with the specified key indefinitely.
     *
     * @param key the key of the value to store
     * @param value the value to store
     * or `null` if the value should be stored indefinitely
     */
    operator fun set(key: String, value: V) = set(key, value, null)

    /**
     * Stores the value in the cache with the specified key.
     * If a duration is specified, the value will be automatically removed from the cache after the specified duration.
     * Otherwise, the value will be stored indefinitely.
     *
     * @param key the key of the value to store
     * @param value the value to store
     * @param duration the duration after which the value will be removed from the cache,
     * or `null` if the value should be stored indefinitely
     */
    fun set(key: String, value: V, duration: Duration? = null) {
        cache[key] = TimedValue(value, duration?.let { System.currentTimeMillis() + it.inWholeMilliseconds })
        pendingWrite?.cancel(false)
        pendingWrite = executor.submit {
            file.parent.createDirectories()
            file.toFile().writeText(JSON_MAPPER.writeValueAsString(cache))
        }
    }

    override fun close() {
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
        executor.shutdownNow()
    }

    companion object {
        private val CACHES = ConcurrentHashMap<String, CacheManager<*>>()

        /**
         * Gets a new [CacheManager] for the specified file and data type.
         * If this is the second time a manager with the same file and data type is requested,
         * the same instance is returned.
         * Otherwise, if the file is the same but the data type is different, a new instance is returned.
         *
         * @param V the type of the value stored in the cache
         * @param file the file where the cache is stored
         * @param valueClass the class of the value stored in the cache
         * @return the cache manager
         */
        @Suppress("UNCHECKED_CAST")
        operator fun <V> get(file: Path, valueClass: Class<V>): CacheManager<V> {
            val key = file.absolutePathString()
            val cache = CACHES[key]
            if (cache == null || cache.valueClass != valueClass) {
                cache?.close()
                val c = CacheManager(file, valueClass)
                CACHES[key] = c
                return c
            }
            return cache as CacheManager<V>
        }

        /**
         * Closes all the managers.
         */
        fun closeAll() {
            CACHES.values.forEach { it.close() }
            CACHES.clear()
        }

    }

    /**
     * Value holder for [CacheManager].
     * Contains a timestamp for expiration.
     *
     * @param V the type of the value
     * @property value the value
     * @property timestamp the expiration date
     * @constructor Creates a new Timed value
     */
    private data class TimedValue<V>(val value: V, val timestamp: Long?) {

        /**
         * Checks if this value has expired.
         *
         * @return true if the value has expired, false otherwise
         */
        @JsonIgnore
        fun isExpired() = timestamp != null && System.currentTimeMillis() > timestamp

    }

}