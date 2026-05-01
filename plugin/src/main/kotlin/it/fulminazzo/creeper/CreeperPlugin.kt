package it.fulminazzo.creeper

import org.gradle.api.Project
import org.gradle.api.Plugin
import java.nio.file.Path

/**
 * A simple 'hello world' plugin.
 */
class CreeperPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        // Register a task
        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello, world!")
            }
        }
    }

    companion object {
        /**
         * The global cache directory.
         */
        internal val CACHE_DIRECTORY
            get() = Path.of(System.getProperty("user.home"), ".gradle", "caches", ProjectInfo.NAME)

    }

}
