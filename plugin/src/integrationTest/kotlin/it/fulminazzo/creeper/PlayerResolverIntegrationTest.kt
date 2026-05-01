package it.fulminazzo.creeper

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
    )

    @Test
    fun `test that getPlayerIds of online returns correct value`() {
        val ids = resolver.getPlayersIds(listOf(CACHED_NAME), true)
        assertContains(
            ids,
            CACHED_ID,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getPlayerIds of offline returns correct value`() {
        val expected = UUID.nameUUIDFromBytes("OfflinePlayer:$CACHED_NAME".toByteArray())
        val ids = resolver.getPlayersIds(listOf(CACHED_NAME), false)
        assertContains(
            ids,
            expected,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getOnlinePlayersIds downloads from API`() {
        val ids = resolver.getOnlinePlayersIds(listOf(UNCACHED_NAME))
        assertContains(
            ids,
            UNCACHED_ID,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getOnlinePlayersIds pulls from cache if available`() {
        val ids = resolver.getOnlinePlayersIds(listOf(CACHED_NAME))
        assertContains(
            ids,
            CACHED_ID,
            "Expected player id not found in list: ${ids.joinToString(", ")}"
        )
    }

    @Test
    fun `test that getOnlinePlayersIds does not throw if no player is found`() {
        val ids = resolver.getOnlinePlayersIds(listOf("not-found"))
        assertTrue(ids.isEmpty(), "Expected empty list but got: ${ids.joinToString(", ")}")
    }

    private companion object {
        private val UNCACHED_ID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
        private const val UNCACHED_NAME = "Notch"

        private val CACHED_ID = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6")
        private const val CACHED_NAME = "jeb_"

        @JvmStatic
        @BeforeAll
        fun setup() {
            val cacheFile = PlayerResolver.CACHE_FILE
            cacheFile.deleteIfExists()
            cacheFile.parent.createDirectories()
            cacheFile.writeText("{\"$CACHED_NAME\":\"$CACHED_ID\"}")
        }

    }

}