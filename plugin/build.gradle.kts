plugins {
    `java-gradle-plugin`

    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins.create("creeper") {
        id = "it.fulminazzo.creeper"
        implementationClass = "it.fulminazzo.creeper.CreeperPlugin"
    }
}

tasks.test {
    useJUnitPlatform()
}

/**
 * FUNCTIONAL TESTS CONFIGURATION
 */
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    description = "Runs the functional test suite."
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}
