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

        configurations["${sourceSetName}Implementation"].extendsFrom(configurations["testImplementation"])
        configurations["${sourceSetName}RuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

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