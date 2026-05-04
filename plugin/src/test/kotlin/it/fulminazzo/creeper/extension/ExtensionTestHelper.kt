package it.fulminazzo.creeper.extension

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder

abstract class ExtensionTestHelper {
    protected val project: Project = ProjectBuilder.builder().build()
    protected val objects: ObjectFactory = project.objects

}