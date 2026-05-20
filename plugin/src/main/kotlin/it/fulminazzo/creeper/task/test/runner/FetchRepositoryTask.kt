package it.fulminazzo.creeper.task.test.runner

import it.fulminazzo.creeper.ProjectInfo
import it.fulminazzo.creeper.download.Downloader
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Task to fetch the repository of this project.
 *
 * @constructor Creates a new Repository fetcher task
 */
abstract class FetchRepositoryTask : DefaultTask() {

    @get:ServiceReference("baseDownloader")
    abstract val downloader: Property<Downloader>

    @get:OutputDirectory
    abstract val repositoryDirectory: RegularFileProperty

    @TaskAction
    fun run() {
        val output = repositoryDirectory.get().asFile
        logger.lifecycle("Fetching repository from $REPOSITORY_URL")
        downloader.get().download(REPOSITORY_URL, output.toPath())
    }

    private companion object {
        private const val REPOSITORY_URL =
            "https://codeload.github.com/fulminazzo/${ProjectInfo.NAME}/zip/refs/heads/master"

    }

}