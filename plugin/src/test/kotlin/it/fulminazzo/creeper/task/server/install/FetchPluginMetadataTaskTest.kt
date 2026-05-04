package it.fulminazzo.creeper.task.server.install

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.fulminazzo.creeper.provider.plugin.PluginProvider
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import it.fulminazzo.creeper.service.provider.plugin.PluginProviderService
import java.io.File
import kotlin.test.Test

class FetchPluginMetadataTaskTest : InstallTaskTestHelper() {

    @Test
    fun `test that FetchPluginMetadataTask calls provider#getName`() {
        val request = mockk<PluginRequest>()
        val pluginMetadata = File("build/resources/test/tmp/plugins/creeper/plugin.info").absoluteFile

        val provider = mockk<PluginProvider<PluginRequest>>()
        every { provider.getName(any()) } returns "creeper"

        val task = createTask(FetchPluginMetadataTask::class.java) { task ->
            task.pluginProviderService.set(createService<PluginProviderService> {
                every { it.provider } returns provider
            })
            task.request.set(request)
            task.pluginMetadata.set(pluginMetadata)
        }

        task.run()

        verify(exactly = 1) { provider.getName(request) }
    }

}