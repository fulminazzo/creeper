package it.fulminazzo.creeper.task.server.install

import io.mockk.*
import it.fulminazzo.creeper.PlayerResolver
import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.service.PlayerResolverService
import java.io.File
import kotlin.test.Test

class WriteFileTaskTest : InstallTaskTestHelper() {

    @Test
    fun `test that WriteFileTask calls on action#apply`() {
        val playerResolver = mockk<PlayerResolver>()
        val specification = mockk<ServerSpec<*, *>>()
        val file = File("build/resources/test/tmp").absoluteFile

        val action = mockk<FileAction>()
        every { action.apply(any(), any(), any()) } just Runs

        val task = createTask(WriteFileTask::class.java) { task ->
            task.playerResolverService.set(createService<PlayerResolverService> {
                every { it.playerResolver } returns playerResolver
            })
            task.action.set(action)
            task.specification.set(specification)
            task.file.set(file)
        }

        task.run()

        verify(exactly = 1) { action.apply(file.toPath().parent, specification, playerResolver) }
    }

}