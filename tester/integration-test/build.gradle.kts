plugins {
    `java-library`
    groovy
    alias(libs.plugins.kotlin)
    scala
}

dependencies {
    api(libs.bundles.test.frameworks)
}