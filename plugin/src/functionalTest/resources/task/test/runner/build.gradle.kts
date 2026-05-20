import it.fulminazzo.creeper.service.downloader.DownloaderService
import it.fulminazzo.creeper.task.test.runner.FetchRepositoryTask
import it.fulminazzo.creeper.task.test.runner.SetupTestRunnerRegistrar

plugins {
    java
    id("it.fulminazzo.creeper")
}

val sharedServices = gradle.sharedServices
sharedServices.registerIfAbsent("simpleDownloaderService", DownloaderService::class.java)
SetupTestRunnerRegistrar.register(project, File(project.projectDir, "build").toPath())

tasks.withType<FetchRepositoryTask>() {
    branch = "plugin-test-runner"
}
