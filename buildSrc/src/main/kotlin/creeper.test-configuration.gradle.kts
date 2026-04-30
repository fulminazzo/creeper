/**
 * Test configuration plugin.
 *
 * Creates test source sets and related tasks.
 */
plugins {
    `java-gradle-plugin`
}

interface TestConfigurationExtension {

    /**
     * Sets the test types to be created.
     */
    val testTypes: SetProperty<String>

    /**
     * The name of the main source set
     */
    val mainSourceSet: Property<String>

    /**
     * Adds a new test type.
     *
     * @param testType the test type
     */
    fun testType(testType: String) {
        testTypes.add(testType)
    }

}

val extension = extensions.create<TestConfigurationExtension>("testConfiguration")

afterEvaluate {

    extension.testTypes.getOrElse(setOf()).forEach { testType ->
        val sourceSetName = "${testType}Test"
        val testSourceSet = sourceSets.create(sourceSetName) {}

        val mainSourceSetName = extension.mainSourceSet.getOrElse("main")
        val mainSourceSet = sourceSets.getByName(mainSourceSetName)

        testSourceSet.compileClasspath += mainSourceSet.output
        testSourceSet.runtimeClasspath += mainSourceSet.output

        configurations["${sourceSetName}Implementation"].extendsFrom(configurations["testImplementation"])
        configurations["${sourceSetName}RuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

        // Enables internal visibility
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            val kotlin = extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java)

            val mainCompilation = kotlin.target.compilations.getByName(mainSourceSetName)
            val testCompilation = kotlin.target.compilations.getByName(sourceSetName)

            testCompilation.associateWith(mainCompilation)
        }

        val task = tasks.register<Test>(sourceSetName) {
            description = "Runs the $testType test suite."
            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath
            useJUnitPlatform()
        }

        gradlePlugin.testSourceSets.add(testSourceSet)

        tasks.named("check") {
            dependsOn(task)
        }

    }

}