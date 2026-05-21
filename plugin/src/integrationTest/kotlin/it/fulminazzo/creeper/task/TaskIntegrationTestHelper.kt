package it.fulminazzo.creeper.task

import io.mockk.mockk
import it.fulminazzo.creeper.task.server.InstallServerRunnerTask
import it.fulminazzo.creeper.task.server.install.InjectTestRunnerRequestTask
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.services.BuildService
import org.gradle.testfixtures.ProjectBuilder

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
            InjectTestRunnerRequestTask.testMode()
            InstallServerRunnerTask.testMode()
        }

    }

}