plugins {
    `java-library`
    groovy
    id("org.jetbrains.kotlin.jvm")
    scala
}

afterEvaluate {
    val integrationTestImplementation by configurations.getting {}

    val functionalTestImplementation by configurations.getting {}
    val functionalTestRuntimeClasspath by configurations

    dependencies {
        compileOnly(libs.slf4j)
        compileOnly(libs.gson)
        compileOnly(libs.yaml)

        api(libs.junit.launcher)
        api(libs.bundles.test.engines)

        integrationTestImplementation(libs.bundles.test.frameworks)
        functionalTestImplementation(libs.bundles.test.frameworks)
    }

    tasks.named<Test>("integrationTest") {
        listOf("Groovy", "Java", "Kotlin", "Scala").forEach {
            dependsOn("compileFunctionalTest$it")
        }

        /*
         * This ugly workaround is necessary due to JUnit loading Kotest
         * with a different classloader than ours in TestRunnerIntegrationTest.
         */
        val classPath = functionalTestRuntimeClasspath.resolvedConfiguration
            .resolvedArtifacts
            .map { it.file }
        systemProperty(
            "functionaltest.framework.classpath",
            classPath.joinToString(File.pathSeparator)
        )
        systemProperty("kotest.framework.classpath.scanning.autoscan.disable", true)
    }

    configurations.named("integrationTestCompileClasspath") {
        exclude(group = "io.kotest", module = "kotest-runner-junit5")
    }

    configurations.named("integrationTestRuntimeClasspath") {
        exclude(group = "io.kotest", module = "kotest-runner-junit5")
    }

}
