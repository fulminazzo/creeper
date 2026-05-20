package it.fulminazzo.creeper.task.test.runner

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.service.downloader.DownloaderService
import it.fulminazzo.creeper.util.ZipUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task to fetch the latest version of this project repository from [GitHub](https://github.com/fulminazzo/creeper).
 * The main purpose of this task is to provide access to the `tester` module to inject a specialized plugin
 * into server platforms that will then run the requested tests.
 *
 * @constructor Creates a new Fetch repository task
 */
abstract class FetchRepositoryTask : DefaultTask() {

    @get:ServiceReference("simpleDownloaderService")
    abstract val downloader: Property<DownloaderService>

    @get:OutputDirectory
    abstract val repositoryDirectory: RegularFileProperty

    @TaskAction
    fun run() {
        val repository = repositoryDirectory.get().asFile
        logger.lifecycle("Fetching repository from $REPOSITORY_URL into ${repository.path}")
        val parentFile = repository.parentFile
        val output = File(parentFile, "tmp.zip")
        downloader.get().downloader.download(REPOSITORY_URL, output.toPath())
        ZipUtils.unzip(output.toPath(), parentFile.toPath())
        output.delete()
        parentFile.resolve("${ProjectInfo.NAME}-master").renameTo(repository)
    }

    private companion object {
        private const val REPOSITORY_URL =
            "https://codeload.github.com/fulminazzo/${ProjectInfo.NAME}/zip/refs/heads/master"

    }

}