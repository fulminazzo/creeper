//TODO: re-enable
//package it.fulminazzo.creeper.server
//
//import it.fulminazzo.creeper.download.CachedDownloader
//import it.fulminazzo.creeper.download.Downloader
//import it.fulminazzo.creeper.provider.MCJarsApiProvider
//import it.fulminazzo.creeper.server.installer.MinecraftServerInstaller
//import it.fulminazzo.creeper.server.runner.MinecraftServerRunner
//import it.fulminazzo.creeper.server.spec.MinecraftServerSpecBuilder
//import it.fulminazzo.creeper.extension.spec.settings.Difficulty
//import it.fulminazzo.creeper.util.VersionUtils
//import org.gradle.jvm.toolchain.JavaLanguageVersion
//import org.gradle.jvm.toolchain.JavaToolchainService
//import org.gradle.testfixtures.ProjectBuilder
//import org.junit.jupiter.api.AfterAll
//import org.gradle.api.logging.Logging
//import java.nio.file.Path
//import java.util.concurrent.Executors
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//import kotlin.time.Duration.Companion.seconds
//
//class MinecraftServerRunnerFunctionalTest {
//
//    @Test
//    fun `test that server configuration correctly installs and runs server`() {
//        val project = ProjectBuilder.builder().build()
//        project.plugins.apply("java")
//        val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)
//
//        // Specification
//        val specificationBuilder = MinecraftServerSpecBuilder()
//        specificationBuilder.type = ServerType.PAPER
//        specificationBuilder.version = "1.16.5"
//        specificationBuilder.whitelist("Fulminazzo")
//        specificationBuilder.whitelist("xca_mux")
//        specificationBuilder.op("xca_mux")
//        specificationBuilder.serverConfig {
//            eula = true
//            port = 25567
//            players = 5
//            whitelist = true
//            difficulty = Difficulty.PEACEFUL
//            onlineMode = false
//        }
//        val specification = specificationBuilder.build()
//
//        // Installer
//        val downloader = CachedDownloader.global(Downloader.http())
//        val provider = MCJarsApiProvider(downloader, LOGGER)
//
//        val installer = MinecraftServerInstaller(
//            specification,
//            LOGGER,
//            EXECUTOR,
//            downloader,
//            provider,
//            provider
//        )
//        val executableDir = installer.install(BASE_DIRECTORY)
//
//        // Runner
//        val launcher = toolchainService.launcherFor {
//            val version = VersionUtils.getJavaVersion(specification.version).toString()
//            it.languageVersion.set(JavaLanguageVersion.of(version))
//        }
//        val javaExecutable = launcher.get().executablePath.toString()
//
//        val runner = MinecraftServerRunner(specification, LOGGER, EXECUTOR, executableDir, javaExecutable)
//        val pid = runner.start()
//        try {
//            runner.awaitCompleteBoot(60.seconds)
//        } finally {
//            runner.forceStop()
//        }
//        assertEquals(137, runner.await(), "Server should have exited with SIG_KILL")
//
//        assertFalse(ProcessHandle.of(pid).isPresent, "Process $pid should have been stopped after forceStop")
//    }
//
//    private companion object {
//        private val LOGGER = Logging.getLogger(MinecraftServerRunnerFunctionalTest::class.java)
//        private val EXECUTOR = Executors.newCachedThreadPool()
//        private val BASE_DIRECTORY = Path.of("build/resources/functionalTest/server/runner")
//
//        @JvmStatic
//        @AfterAll
//        fun tearDown() {
//            EXECUTOR.close()
//        }
//
//    }
//
//}