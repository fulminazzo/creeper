package it.fulminazzo.creeper

import org.gradle.api.Project
import org.gradle.api.Plugin

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
}
