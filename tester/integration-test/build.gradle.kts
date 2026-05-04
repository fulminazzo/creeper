plugins {
    `java-library`
    groovy
    alias(libs.plugins.kotlin)
    scala
}

val integrationTestImplementation by configurations.getting {}

dependencies {

    integrationTestImplementation(libs.bundles.test.frameworks)
}