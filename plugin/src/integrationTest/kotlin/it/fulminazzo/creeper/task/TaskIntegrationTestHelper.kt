package it.fulminazzo.creeper.task

import io.mockk.mockk
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.provider.plugin.LocalPluginRequest
import it.fulminazzo.creeper.task.server.install.InjectTestRunnerDependencyTask
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.services.BuildService
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

abstract class TaskIntegrationTestHelper {
    protected val project = ProjectBuilder.builder().build()

    protected fun <T : Task> createTask(type: Class<T>, configurationAction: Action<T> = {}): T =
        project.tasks.register(type.simpleName.lowercase(), type, configurationAction).get()

    protected inline fun <reified S : BuildService<*>> createService(configurationAction: Action<S> = {}): S {
        val service = mockk<S>()
        configurationAction.execute(service)
        return service
    }

    private companion object {

        init {
            InjectTestRunnerDependencyTask.testMode()
        }

    }

}