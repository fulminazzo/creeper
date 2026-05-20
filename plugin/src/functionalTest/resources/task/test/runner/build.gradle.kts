import it.fulminazzo.creeper.service.downloader.DownloaderService
import it.fulminazzo.creeper.task.test.runner.SetupTestRunnerRegistrar

plugins {
    id("it.fulminazzo.creeper")
}

val sharedServices = gradle.sharedServices
sharedServices.registerIfAbsent("simpleDownloaderService", DownloaderService::class.java)
SetupTestRunnerRegistrar.register(project, File(project.projectDir, "build").toPath())