plugins {
    `java-library`
    groovy
    alias(libs.plugins.kotlin)
    scala
}

val integrationTestImplementation by configurations.getting {}

dependencies {
    compileOnly(libs.slf4j)
    compileOnly(libs.gson)
    compileOnly(libs.yaml)

    api(libs.junit.launcher)
    api(libs.bundles.test.engines)

    integrationTestImplementation(libs.bundles.test.frameworks)
}
