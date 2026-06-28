plugins {
    `java-library`
    groovy
    id("org.jetbrains.kotlin.jvm")
    scala
}

afterEvaluate {
    val integrationTestImplementation by configurations.getting {}
    val functionalTestImplementation by configurations.getting {}

    dependencies {
        compileOnly(libs.slf4j)
        compileOnly(libs.gson)
        compileOnly(libs.yaml)

        api(libs.junit.launcher)
        api(libs.bundles.test.engines)

        integrationTestImplementation(libs.bundles.test.frameworks)
        functionalTestImplementation(libs.bundles.test.frameworks)
    }

    tasks.named("integrationTest") {
        listOf("Groovy", "Java", "Kotlin", "Scala").forEach {
            dependsOn("compileFunctionalTest$it")
        }
    }

}
