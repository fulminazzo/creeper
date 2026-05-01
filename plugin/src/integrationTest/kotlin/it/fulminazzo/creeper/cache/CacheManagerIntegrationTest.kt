package it.fulminazzo.creeper.cache

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class CacheManagerIntegrationTest {
    private lateinit var manager: CacheManager<Data>

    @BeforeEach
    fun setup() {
        manager = CacheManager.get(FILE, Data::class.java)
    }

    @Test
    fun `test that stores data`() {
        val sleepTime = 750L

        FILE.deleteIfExists()
        val expectedFirst = Data("1", "one")
        manager.set("1", expectedFirst, (sleepTime * 2).milliseconds)
        val expectedSecond = Data("2", "two")
        manager["2"] = expectedSecond

        Thread.sleep(sleepTime)
        assertTrue(FILE.exists(), "Cache file does not exist: $FILE")
        assertContains(FILE.toFile().readText(), """"value":{"id":"1","name":"one"}""")

        val actualFirst = manager["1"]
        assertEquals(expectedFirst, actualFirst, "Cached data does not match expected data")
        val actualSecond = manager["2"]
        assertEquals(expectedSecond, actualSecond, "Cached data does not match expected data")

        Thread.sleep(sleepTime)
        val actualFirst2 = manager["1"]
        assertNull(actualFirst2, "Cached data was not removed after being expired")
        val actualSecond2 = manager["2"]
        assertEquals(expectedSecond, actualSecond2, "Cached data does not match expected data")
    }

    @Test
    fun `test that get method does not recreate cache if class matches`() {
        val first = CacheManager.get(FILE, Data::class.java)
        val second = CacheManager.get(FILE, Data::class.java)
        assertEquals(first, second, "Cache instances are not the same")
    }

    @Test
    fun `test that get method recreates cache if class does not match`() {
        val first = CacheManager.get(FILE, String::class.java)
        val second = CacheManager.get(FILE, Data::class.java)
        assertNotEquals<CacheManager<*>>(first, second, "Cache instances are the same")
    }

    private companion object {
        private val FILE = Path.of("build/resources/integrationTest/cache/cache_manager_test.json")

        @JvmStatic
        @AfterAll
        fun tearDown() {
            CacheManager.closeAll()
        }

    }

}

data class Data(val id: String, val name: String)