plugins {
    `java-library`
}

val implementationDependencies: List<String> by rootProject

val integrationTestImplementation by configurations.getting {}

dependencies {
    compileOnly(libs.slf4j)
    compileOnly(libs.gson)

    api(libs.junit.launcher)
    api(libs.bundles.test.engines)
    implementationDependencies.forEach { api(it) }

    integrationTestImplementation(project(":integration-test"))
}