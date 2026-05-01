package it.fulminazzo.creeper

import it.fulminazzo.creeper.cache.CacheManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class PlayerResolverIntegrationTest {
    private val resolver = PlayerResolver(
        LoggerFactory.getLogger(PlayerResolverIntegrationTest::class.java)
    ) { it.run() }

    @Test
    fun `test that getPlayerProfiles of online returns correct value`() {
        val ids = resolver.getPlayerProfiles(listOf(CACHED.name), true)
        assertContains(
            ids,
            CACHED,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getPlayerProfiles of offline returns correct value`() {
        val name = CACHED.name
        val expected = PlayerProfile(
            UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray()),
            name
        )
        val ids = resolver.getPlayerProfiles(listOf(name), false)
        assertContains(
            ids,
            expected,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getOnlinePlayerProfiles downloads from API`() {
        val ids = resolver.getOnlinePlayerProfiles(listOf(UNCACHED.name))
        assertContains(
            ids,
            UNCACHED,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getOnlinePlayerProfiles pulls from cache if available`() {
        val ids = resolver.getOnlinePlayerProfiles(listOf(CACHED.name))
        assertContains(
            ids,
            CACHED,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getOnlinePlayerProfiles does not throw if no player is found`() {
        val ids = resolver.getOnlinePlayerProfiles(listOf("not-found"))
        assertTrue(ids.isEmpty(), "Expected empty list but got: ${ids.joinToString(", ")}")
    }

    private companion object {
        private val UNCACHED = PlayerProfile(
            UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"),
            "Notch"
        )

        private val CACHED = PlayerProfile(
            UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"),
            "jeb_"
        )

        @JvmStatic
        @BeforeAll
        fun setup() {
            val cacheFile = PlayerResolver.CACHE_FILE
            cacheFile.deleteIfExists()
            cacheFile.parent.createDirectories()
            cacheFile.writeText("""{
                |"${CACHED.name}": {
                |   "value": "${CACHED.id}",
                |   "timestamp": ${System.currentTimeMillis()}
                |}
                |}""".trimMargin())
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            CacheManager.closeAll()
        }

    }

}