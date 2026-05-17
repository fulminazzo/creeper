package it.fulminazzo.creeper.task.server

import io.mockk.every
import io.mockk.mockk
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

abstract class RegistrarTestHelper {
    protected val specification = mockk<MinecraftServerSpec>()

    protected val project = ProjectBuilder.builder().build()

    protected val taskBaseName = "Bukkit26_1"
    protected val serverDisplayName = "Bukkit 26.1"

    @BeforeEach
    fun setup() {
        every { specification.id } returns SERVER_ID
        every { specification.type.name } returns SERVER_NAME
        every { specification.version } returns SERVER_VERSION
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Task> testTaskMetadata(taskName: String, shown: Boolean): Pair<String, T> {
        val t = project.tasks.findByName(taskName)
        assertNotNull(t, "Could not find task $taskName")
        val task = t as T
        val taskGroup = task.group
        if (shown) assertEquals(
            serverDisplayName,
            taskGroup,
            "Task $taskName should have group $serverDisplayName ($taskGroup)"
        )
        else assertNull(taskGroup, "Task $taskName should not have a group ($taskGroup)")
        assertNotNull(task.description, "Task $taskName should have a description")
        return Pair(taskName, task)
    }

    protected fun testDependency(first: Task, second: Task) =
        assertContains(
            first.taskDependencies.getDependencies(first),
            second,
            "Task ${first.name} should depend on ${second.name}"
        )

    protected fun getTask(taskName: String): Task {
        val task = project.tasks.findByName(taskName)
        assertNotNull(task, "Could not find task $taskName")
        return task
    }

    protected fun setupTask(): Task {
        val task = mockk<Task>()
        every { task.dependsOn(any()) } returns task
        every { task.name } returns "mock"
        return task
    }

    protected companion object {
        protected const val SERVER_ID = "bukkit-26.1"
        protected const val SERVER_NAME = "Bukkit"
        protected const val SERVER_VERSION = "26.1"

        @JvmStatic
        protected val SERVER_DIRECTORY: Path = Path.of("build/server/bukkit-26.1")

    }

}