package it.fulminazzo.creeper.task.server.install

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.provider.ConfigProvider
import it.fulminazzo.creeper.service.provider.ConfigProviderService
import java.io.File
import java.nio.file.Files
import kotlin.test.Test

class InstallConfigTaskTest : InstallTaskTestHelper() {

    @Test
    fun `test that InstallConfigTask calls provider#get and action#apply`() {
        val type = ServerType.PAPER
        val version = "1.21"
        val configFile = File("build/resources/test/tmp/server.properties").absoluteFile

        val specification = mockk<ServerSpec<*, *>>()
        every { specification.type } returns type
        every { specification.version } returns version

        val tempPath = Files.createTempFile("server", ".properties")
        val provider = mockk<ConfigProvider>()
        every { provider.get(any(), any(), any(), any()) } returns tempPath

        val action = mockk<ConfigAction>()
        every { action.apply(any(), any()) } just Runs

        val task = createTask(InstallConfigTask::class.java) { task ->
            task.configProviderService.set(createService<ConfigProviderService> {
                every { it.provider } returns provider
            })
            task.action.set(action)
            task.specification.set(specification)
            task.configFile.set(configFile)
        }

        task.run()

        verify(exactly = 1) { provider.get(configFile.name, type, version, configFile.parentFile.toPath()) }
        verify(exactly = 1) { action.apply(mutableMapOf(), specification) }
    }

}