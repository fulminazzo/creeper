package it.fulminazzo.creeper.task

import io.mockk.every
import it.fulminazzo.creeper.download.CachedDownloader
import it.fulminazzo.creeper.download.Downloader
import it.fulminazzo.creeper.provider.plugin.PluginProvider
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.provider.plugin.RedirectPluginProvider
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import it.fulminazzo.creeper.task.server.InstallServerRunnerTask
import kotlin.test.Test
import kotlin.test.assertTrue

class InstallServerRunnerTaskIntegrationTest : TaskIntegrationTestHelper() {

    @Test
    fun `test that InstallServerRunnerTask correctly installs server runner`() {
        val provider: PluginProvider<PluginRequest> = RedirectPluginProvider(
            project.logger,
            CachedDownloader.simple(Downloader.http())
        )

        val serverRunner = project.file("build/server/${InstallServerRunnerTask.FILE_NAME}")
        val task = createTask(InstallServerRunnerTask::class.java) {
            it.serverRunner.set(serverRunner)
        }
        task.pluginProviderService.set(createService<PluginProviderService> {
            every { it.provider } returns provider
        })

        task.run()

        assertTrue(serverRunner.exists(), "Server runner file does not exist")
    }

}