package it.fulminazzo.creeper.download

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.fulminazzo.creeper.util.sha256
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCachedDownloaderTest {

    @Test
    fun `test that downloader caches requests and only downloads once`() {
        val delegate = mockk<Downloader>()
        every { delegate.download(any(), any()) } answers {
            Thread.sleep(1_000)
        }

        val scope = CoroutineScope(Dispatchers.IO)

        val downloader = CachedDownloader.global(delegate, scope)

        val resource = "https://www.google.com"
        val path = Path.of("build/resources/test/download/global_cached_downloader_test.txt")
        val hash = "1234567890"

        val first = downloader.download(resource, path, hash)
        val second = downloader.download(resource, path, hash)

        assertEquals(first, second, "downloads were not equal")
        verify(exactly = 1) { delegate.download(resource, any()) }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            // HTTPS, elidable port, extension, no query
            "https://www.google.com:443/index.html,https://www.google.com/index.html",
            // HTTP, elidable port, extension, no query
            "http://www.google.com:80/index.html,http://www.google.com/index.html",
            // HTTPS, elidable port, no extension, no query
            "https://www.google.com:443/index,https://www.google.com/index",
            // HTTP, elidable port, no extension, no query
            "http://www.google.com:80/index,http://www.google.com/index",
            // HTTPS, elidable port, extension, query
            "https://www.google.com:443/index.html?test=true&valid=false,https://www.google.com/index.html?test=true&valid=false",
            // HTTP, elidable port, extension, query
            "http://www.google.com:80/index.html?test=true&valid=false,http://www.google.com/index.html?test=true&valid=false",
            // HTTPS, elidable port, no extension, query
            "https://www.google.com:443/index?test=true&valid=false,https://www.google.com/index?test=true&valid=false",
            // HTTP, elidable port, no extension, query
            "http://www.google.com:80/index?test=true&valid=false,http://www.google.com/index?test=true&valid=false",
            // HTTPS, port, extension, no query
            "https://www.google.com:8080/index.html,https://www.google.com:8080/index.html",
            // HTTP, port, extension, no query
            "http://www.google.com:8080/index.html,http://www.google.com:8080/index.html",
            // HTTPS, port, no extension, no query
            "https://www.google.com:8080/index,https://www.google.com:8080/index",
            // HTTP, port, no extension, no query
            "http://www.google.com:8080/index,http://www.google.com:8080/index",
            // HTTPS, port, extension, query
            "https://www.google.com:8080/index.html?test=true&valid=false,https://www.google.com:8080/index.html?test=true&valid=false",
            // HTTP, port, extension, query
            "http://www.google.com:8080/index.html?test=true&valid=false,http://www.google.com:8080/index.html?test=true&valid=false",
            // HTTPS, port, no extension, query
            "https://www.google.com:8080/index?test=true&valid=false,https://www.google.com:8080/index?test=true&valid=false",
            // HTTP, port, no extension, query
            "http://www.google.com:8080/index?test=true&valid=false,http://www.google.com:8080/index?test=true&valid=false",
            // HTTPS, no port, extension, no query
            "https://www.google.com/index.html,https://www.google.com/index.html",
            // HTTP, no port, extension, no query
            "http://www.google.com/index.html,http://www.google.com/index.html",
            // HTTPS, no port, no extension, no query
            "https://www.google.com/index,https://www.google.com/index",
            // HTTP, no port, no extension, no query
            "http://www.google.com/index,http://www.google.com/index",
            // HTTPS, no port, extension, query
            "https://www.google.com/index.html?test=true&valid=false,https://www.google.com/index.html?test=true&valid=false",
            // HTTP, no port, extension, query
            "http://www.google.com/index.html?test=true&valid=false,http://www.google.com/index.html?test=true&valid=false",
            // HTTPS, no port, no extension, query
            "https://www.google.com/index?test=true&valid=false,https://www.google.com/index?test=true&valid=false",
            // HTTP, no port, no extension, query
            "http://www.google.com/index?test=true&valid=false,http://www.google.com/index?test=true&valid=false",
            // HTTPS, no port, no extension, no path, no query
            "https://www.google.com/,https://www.google.com/",
            "https://www.google.com,https://www.google.com",
            // HTTP, no port, no extension, no path, no query
            "http://www.google.com/,http://www.google.com/",
            "http://www.google.com,http://www.google.com",
        ]
    )
    fun `test that hashUrl correctly hashes`(url: String, expected: String) {
        var actualExpected = expected
        val regex = """(https?://www.google.com(?::[0-9]+)?)(/.*)?""".toRegex()
        regex.find(actualExpected)?.let {
            actualExpected = it.groupValues[1].sha256() + it.groupValues[2]
        }
        assertEquals(
            actualExpected,
            CachedDownloader.GlobalCachedDownloader.hashUrl(url),
            "hash was not equal for expected url: $expected"
        )
    }

}