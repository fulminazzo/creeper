package it.fulminazzo.creeper.task.server.install

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.ServerType
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.provider.plugin.PluginRequest
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InstallServerTaskRegistrarTest {
    private val specification = mockk<MinecraftServerSpec>()

    private val project = ProjectBuilder.builder().build()

    private val taskBaseName = "Bukkit26_1"
    private val serverDisplayName = "Bukkit 26.1"

    @BeforeEach
    fun setup() {
        every { specification.id } returns SERVER_ID
        every { specification.type.name } returns SERVER_NAME
        every { specification.version } returns SERVER_VERSION
    }

    @Test
    fun `test that register correctly registers all tasks and dependency hierarchy`() {
        val pluginRequest = mockk<PluginRequest>()
        every { pluginRequest.toHashString() } returns "hash"

        val specification = mockk<MinecraftServerSpec>()
        every { specification.id } returns SERVER_ID
        every { specification.type } returns ServerType.BUKKIT
        every { specification.version } returns SERVER_VERSION
        every { specification.plugins } returns listOf(pluginRequest, pluginRequest)
        InstallServerTaskRegistrar.register(project, specification, SERVER_DIRECTORY.parent)

        val (_, baseTask) = testTaskMetadata<Task>("install${taskBaseName}", true)

        val executableTask = getTask("install${taskBaseName}Executable")
        assertEquals(
            project.projectDir.toPath().resolve(SERVER_DIRECTORY).toFile(),
            (executableTask as InstallExecutableTask).executable.orNull?.asFile?.parentFile,
            "Executable server directory should be $SERVER_DIRECTORY"
        )

        val pluginsTask = getTask("install${taskBaseName}Plugins")

        val fetchPlugin1MetadataTask = getTask("fetch${taskBaseName}Plugin1Metadata")
        testDependency(fetchPlugin1MetadataTask, executableTask)
        val installPlugin1Task = getTask("install${taskBaseName}Plugin1")
        testDependency(installPlugin1Task, fetchPlugin1MetadataTask)

        val fetchPlugin2MetadataTask = getTask("fetch${taskBaseName}Plugin2Metadata")
        testDependency(fetchPlugin2MetadataTask, executableTask)
        val installPlugin2Task = getTask("install${taskBaseName}Plugin2")
        testDependency(installPlugin2Task, fetchPlugin2MetadataTask)

        testDependency(pluginsTask, installPlugin1Task)
        testDependency(pluginsTask, installPlugin2Task)

        val serverPropertiesTask = getTask("install${taskBaseName}Properties")
        testDependency(serverPropertiesTask, executableTask)
        val writeEulaTask = getTask("write${taskBaseName}Eula")
        testDependency(writeEulaTask, executableTask)
        val writeWhitelistTask = getTask("write${taskBaseName}Whitelist")
        testDependency(writeWhitelistTask, executableTask)
        val writeOperatorsTask = getTask("write${taskBaseName}Operators")
        testDependency(writeOperatorsTask, executableTask)

        val bukkitYmlTask = getTask("install${taskBaseName}BukkitYml")
        testDependency(bukkitYmlTask, executableTask)

        testDependency(baseTask, executableTask)
        testDependency(baseTask, pluginsTask)
        testDependency(baseTask, serverPropertiesTask)
        testDependency(baseTask, writeEulaTask)
        testDependency(baseTask, writeWhitelistTask)
        testDependency(baseTask, writeOperatorsTask)
        testDependency(baseTask, bukkitYmlTask)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerInstallExecutableTask correctly register task`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers, setupExecutableTask = false)
        registrar.registerInstallExecutableTask()

        val (taskName, task) = testTaskMetadata<Task>("install${taskBaseName}Executable", true)
        assertEquals(
            task,
            registrar.executableTask,
            "Task $taskName should be the executableTask"
        )

        verify(exactly = 1) { registrar.baseTask.dependsOn(task) }
    }

    /**
     * PLUGINS
     */

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerInstallPluginsTask correctly register task for plugins`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerInstallPluginsTask()

        val (taskName, task) = testTaskMetadata<Task>("install${taskBaseName}Plugins", true)
        assertEquals(
            task,
            registrar.pluginsTask,
            "Task $taskName should be the pluginsTask"
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerFetchPluginMetadataTask correctly register task for plugin`(showWorkers: Boolean) {
        val request = mockk<PluginRequest>()
        every { request.toHashString() } returns "hash"
        val index = 1

        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerFetchPluginMetadataTask(request, index)

        val (taskName, task) = testTaskMetadata<FetchPluginMetadataTask>(
            "fetch${taskBaseName}Plugin${index}Metadata",
            showWorkers
        )

        assertEquals(
            request,
            task.request.orNull,
            "Task $taskName should have request $request"
        )
        val pluginsDirectory = project.projectDir.toPath().resolve(SERVER_DIRECTORY).resolve("plugins")
        val pluginMetadataFile = pluginsDirectory.resolve("${ProjectInfo.NAME}/plugins/hash.info")
        assertEquals(
            pluginMetadataFile.toFile(),
            task.pluginMetadata.orNull?.asFile,
            "Task $taskName should have pluginMetadata pointing to $pluginMetadataFile"
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerInstallPluginTask correctly register task for plugin`(showWorkers: Boolean) {
        val request = mockk<PluginRequest>()
        every { request.toHashString() } returns "hash"
        val index = 1

        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerInstallPluginTask(request, index)

        val (taskName, task) = testTaskMetadata<InstallPluginTask>(
            "install${taskBaseName}Plugin$index",
            showWorkers
        )

        assertEquals(
            request,
            task.request.orNull,
            "Task $taskName should have request $request"
        )
        val pluginsDirectory = project.projectDir.toPath().resolve(SERVER_DIRECTORY).resolve("plugins")
        val pluginMetadataFile = pluginsDirectory.resolve("${ProjectInfo.NAME}/plugins/hash.info")
        assertEquals(
            pluginMetadataFile.toFile(),
            task.pluginMetadata.orNull?.asFile,
            "Task $taskName should have pluginMetadata pointing to $pluginMetadataFile"
        )
        assertEquals(
            pluginsDirectory.toFile(),
            task.pluginsDirectory.orNull?.asFile,
            "Task $taskName should have pluginsDirectory pointing to $pluginsDirectory"
        )
    }

    /**
     * [it.fulminazzo.creeper.ServerType.MinecraftType]
     */

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerInstallProperties correctly registers task for server_properties file`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerInstallProperties()

        val (taskName, task) = testTaskMetadata<InstallConfigTask>(
            "install${taskBaseName}Properties",
            showWorkers
        )

        assertEquals(
            ConfigAction.ServerProperties,
            task.action.orNull,
            "Task $taskName should have action ServerProperties"
        )
        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )
        val configFile = project.projectDir.toPath().resolve(SERVER_DIRECTORY).resolve("server.properties")
        assertEquals(
            configFile.toFile(),
            task.configFile.orNull?.asFile,
            "Task $taskName should have configFile pointing to $configFile"
        )

        testDependencyHierarchy(registrar, task, taskName)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerWriteEula correctly registers task for eula_txt file`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerWriteEula()

        val (taskName, task) = testTaskMetadata<WriteFileTask>(
            "write${taskBaseName}Eula",
            showWorkers
        )

        assertEquals(
            FileAction.Eula,
            task.action.orNull,
            "Task $taskName should have action Eula"
        )
        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )
        val file = project.projectDir.toPath().resolve(SERVER_DIRECTORY).resolve("eula.txt")
        assertEquals(
            file.toFile(),
            task.file.orNull?.asFile,
            "Task $taskName should have file pointing to $file"
        )

        testDependencyHierarchy(registrar, task, taskName)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerWriteWhitelist correctly registers task for whitelist_json file`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerWriteWhitelist()

        val (taskName, task) = testTaskMetadata<WriteFileTask>(
            "write${taskBaseName}Whitelist",
            showWorkers
        )

        assertEquals(
            FileAction.Whitelist,
            task.action.orNull,
            "Task $taskName should have action Whitelist"
        )
        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )
        val file = project.projectDir.toPath().resolve(SERVER_DIRECTORY).resolve("whitelist.json")
        assertEquals(
            file.toFile(),
            task.file.orNull?.asFile,
            "Task $taskName should have file pointing to $file"
        )

        testDependencyHierarchy(registrar, task, taskName)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerWriteOperators correctly registers task for ops_json file`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerWriteOperators()

        val (taskName, task) = testTaskMetadata<WriteFileTask>(
            "write${taskBaseName}Operators",
            showWorkers
        )

        assertEquals(
            FileAction.Operators,
            task.action.orNull,
            "Task $taskName should have action Operators"
        )
        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )
        val file = project.projectDir.toPath().resolve(SERVER_DIRECTORY).resolve("ops.json")
        assertEquals(
            file.toFile(),
            task.file.orNull?.asFile,
            "Task $taskName should have file pointing to $file"
        )

        testDependencyHierarchy(registrar, task, taskName)
    }

    /**
     * [it.fulminazzo.creeper.ServerType.BUKKIT]
     */

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that registerInstallBukkitYml correctly registers task for bukkit_yml file`(showWorkers: Boolean) {
        val registrar = createRegistrar(showWorkers = showWorkers)
        registrar.registerInstallBukkitYml()

        val (taskName, task) = testTaskMetadata<InstallConfigTask>(
            "install${taskBaseName}BukkitYml",
            showWorkers
        )

        assertEquals(
            ConfigAction.BukkitConfig,
            task.action.orNull,
            "Task $taskName should have action BukkitConfig"
        )
        assertEquals(
            specification,
            task.specification.orNull,
            "Task $taskName should have the same specification"
        )
        val configFile = project.projectDir.toPath().resolve(SERVER_DIRECTORY).resolve("bukkit.yml")
        assertEquals(
            configFile.toFile(),
            task.configFile.orNull?.asFile,
            "Task $taskName should have configFile pointing to $configFile"
        )

        testDependencyHierarchy(registrar, task, taskName)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Task> testTaskMetadata(taskName: String, shown: Boolean): Pair<String, T> {
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

    private fun testDependencyHierarchy(registrar: InstallServerTaskRegistrar, task: Task, taskName: String) {
        verify(exactly = 1) { registrar.baseTask.dependsOn(task) }
        testDependency(task, registrar.executableTask)
    }

    private fun testDependency(first: Task, second: Task) =
        assertContains(
            first.taskDependencies.getDependencies(first),
            second,
            "Task ${first.name} should depend on ${second.name}"
        )

    private fun getTask(taskName: String): Task {
        val task = project.tasks.findByName(taskName)
        assertNotNull(task, "Could not find task $taskName")
        return task
    }

    private fun createRegistrar(
        setupBaseTask: Boolean = true,
        setupExecutableTask: Boolean = true,
        setupPluginTask: Boolean = false,
        showWorkers: Boolean = false
    ): InstallServerTaskRegistrar {
        val registrar = InstallServerTaskRegistrar(specification, showWorkers)
        registrar.project = project
        registrar.serverDirectory = SERVER_DIRECTORY
        if (setupBaseTask) registrar.baseTask = setupTask()
        if (setupExecutableTask) registrar.executableTask = setupTask()
        if (setupPluginTask) registrar.pluginsTask = setupTask()
        return registrar
    }

    private fun setupTask(): Task {
        val task = mockk<Task>()
        every { task.dependsOn(any()) } returns task
        every { task.name } returns "mock"
        return task
    }

    private companion object {
        private const val SERVER_ID = "bukkit-26.1"
        private const val SERVER_NAME = "Bukkit"
        private const val SERVER_VERSION = "26.1"

        private val SERVER_DIRECTORY = Path.of("build/server/bukkit-26.1")

    }

}