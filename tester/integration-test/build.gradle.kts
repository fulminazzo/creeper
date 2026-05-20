plugins {
    `java-library`
//    groovy //TODO: missing implementation dependencies
//    alias(libs.plugins.kotlin) //TODO: missing implementation dependencies, weird Kotest exception
//    scala //TODO: missing implementation dependencies
}

val integrationTestImplementation by configurations.getting {}

dependencies {
    integrationTestImplementation(libs.bundles.test.frameworks)
}