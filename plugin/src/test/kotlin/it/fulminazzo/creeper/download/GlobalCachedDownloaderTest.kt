package it.fulminazzo.creeper.download

import it.fulminazzo.creeper.util.sha256
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class GlobalCachedDownloaderTest {
    
    @ParameterizedTest
    @CsvSource(value = [
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
    ])
    fun `test that hashUrl correctly hashes`(url: String, expected: String) {
        assertEquals(
            expected.sha256(),
            CachedDownloader.GlobalCachedDownloader.hashUrl(url),
            "hash was not equal for expected url: $expected"
        )
    }
    
}