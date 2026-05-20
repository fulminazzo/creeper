package it.fulminazzo.creeper.task.test.runner

import io.mockk.every
import it.fulminazzo.creeper.download.Downloader
import it.fulminazzo.creeper.service.downloader.DownloaderService
import it.fulminazzo.creeper.task.TaskIntegrationTestHelper
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class FetchRepositoryTaskIntegrationTest : TaskIntegrationTestHelper() {

    @Test
    fun `test that FetchRepositoryTask correctly downloads and extracts repository`() {
        val task = createTask(FetchRepositoryTask::class.java) { task ->
            task.downloader.set(createService<DownloaderService> { every { it.downloader } returns Downloader.http() })
            task.repositoryDirectory.set(WORK_DIR)
        }

        WORK_DIR.deleteRecursively()
        task.run()

        val workDir = project.projectDir.resolve(WORK_DIR)
        assertTrue(workDir.exists(), "The repository should have been downloaded and extracted")
        val pluginDir = workDir.resolve("plugin")
        assertTrue(pluginDir.exists(), "The plugin directory should have been created")
    }

    private companion object {
        private val WORK_DIR = File("build/resources/integrationTest/task/test/runner/repository")

    }

}