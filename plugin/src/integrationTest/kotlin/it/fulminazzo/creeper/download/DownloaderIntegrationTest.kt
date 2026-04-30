package it.fulminazzo.creeper.download

import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains

class DownloaderIntegrationTest {
    private val downloader = Downloader.http()

    @Test
    fun `test that HTTP downloader works`() {
        DESTINATION_PATH.deleteIfExists()

        downloader.download("https://raw.githubusercontent.com/gradle/gradle/master/gradle.properties", DESTINATION_PATH)
        assertContains(DESTINATION_PATH.readText(), "org.gradle.jvmargs=")
    }

    companion object {
        private val DESTINATION_PATH = Path.of("build/resources/integrationTest/downloader/downloader_test.txt")

    }

}