package it.fulminazzo.creeper.task.server.install

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.fulminazzo.creeper.provider.plugin.PluginProvider
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import java.io.File
import kotlin.test.Test

class InstallPluginTaskTest : InstallTaskTestHelper() {

    @Test
    fun `test that InstallPluginTask calls provider#handleRequest`() {
        val request = mockk<PluginRequest>()
        val plugin = File("build/resources/test/tmp/plugins/plugin.jar").absoluteFile

        val provider = mockk<PluginProvider<PluginRequest>>()
        every { provider.handleRequest(any(), any()) } returns plugin.toPath()

        val task = createTask(InstallPluginTask::class.java) { task ->
            task.pluginProviderService.set(createService<PluginProviderService> {
                every { it.provider } returns provider
            })
            task.request.set(request)
            task.plugin.set(plugin)
        }

        task.run()

        verify(exactly = 1) { provider.handleRequest(request, plugin.parentFile.toPath()) }
    }

}