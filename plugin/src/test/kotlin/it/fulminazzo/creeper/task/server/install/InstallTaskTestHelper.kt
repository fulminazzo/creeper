package it.fulminazzo.creeper.task.server.install

import io.mockk.mockk
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.services.BuildService
import org.gradle.testfixtures.ProjectBuilder

abstract class InstallTaskTestHelper {
    protected val project = ProjectBuilder.builder().build()

    protected fun <T : Task> createTask(type: Class<T>, configurationAction: Action<T> = {}): T =
        project.tasks.register(type.simpleName.lowercase(), type, configurationAction).get()

    protected inline fun <reified S : BuildService<*>> createService(configurationAction: Action<S> = {}): S {
        val service = mockk<S>()
        configurationAction.execute(service)
        return service
    }

}