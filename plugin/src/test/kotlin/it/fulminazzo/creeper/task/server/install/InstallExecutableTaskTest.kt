package it.fulminazzo.creeper.task.server.install

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.provider.JarProvider
import it.fulminazzo.creeper.service.provider.JarProviderService
import java.io.File
import kotlin.test.Test

class InstallExecutableTaskTest : InstallTaskTestHelper() {

    @Test
    fun `test that InstallExecutableTask calls provider#get`() {
        val type = ServerType.PAPER
        val version = "1.21"
        val executable = File("build/resources/test/tmp/server.jar").absoluteFile

        val specification = mockk<ServerSpec<*, *>>()
        every { specification.type } returns type
        every { specification.version } returns version

        val provider = mockk<JarProvider>()
        every { provider.get(any(), any(), any()) } returns executable.toPath()

        val task = createTask(InstallExecutableTask::class.java) { task ->
            task.jarProviderService.set(createService<JarProviderService> {
                every { it.provider } returns provider
            })
            task.specification.set(specification)
            task.executable.set(executable)
        }

        task.run()

        verify(exactly = 1) { provider.get(type, version, executable.parentFile.toPath()) }
    }

}