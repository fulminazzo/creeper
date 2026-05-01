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
        val expected = Data("1", "one")
        manager.set("1", expected, (sleepTime * 2).milliseconds)

        Thread.sleep(sleepTime)
        assertTrue(FILE.exists(), "Cache file does not exist: $FILE")
        assertContains(FILE.toFile().readText(), """"value":{"id":"1","name":"one"}""")

        val actual = manager["1"]
        assertEquals(expected, actual, "Cached data does not match expected data")

        Thread.sleep(sleepTime)
        val actual2 = manager["1"]
        assertNull(actual2, "Cached data was not removed after being expired")
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