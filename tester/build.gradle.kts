plugins {
    java
    groovy
    alias(libs.plugins.kotlin)
    scala
}

// VARIABLES START
val compileJavaVersion = JavaLanguageVersion.of(8)
val gradleJavaVersion = JavaLanguageVersion.of(8)
val implementationDependencies = listOf<String>()
val creeperGroup = "it.fulminazzo.creeper"
val creeperVersion = "0.0.1-SNAPSHOT"
// VARIABLES END

val compileJavaVersionInt = compileJavaVersion.asInt()

group = creeperGroup
version = creeperVersion

java {
    toolchain {
        languageVersion.set(gradleJavaVersion)
    }
}

repositories {
    mavenCentral()
}

/**
 * INTEGRATION TESTS CONFIGURATION
 */
val integrationTestSourceSet = sourceSets.create("integrationTest") {
}

val mainSourceSet = sourceSets.getByName("main")

integrationTestSourceSet.compileClasspath += mainSourceSet.output
integrationTestSourceSet.runtimeClasspath += mainSourceSet.output

configurations["integrationTestCompileOnly"].extendsFrom(configurations["testCompileOnly"])
configurations["integrationTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["integrationTestAnnotationProcessor"].extendsFrom(configurations["testAnnotationProcessor"])
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val integrationTestCompileOnly by configurations.getting {}
val integrationTestImplementation by configurations.getting {}
val integrationTestAnnotationProcessor by configurations.getting {}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs the integration test suite."
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

tasks.check {
    // Run the integration tests as part of `check`
    dependsOn(integrationTest)
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains)

    compileOnly(libs.slf4j)
    compileOnly(libs.gson)

    implementation(libs.junit.launcher)
    implementation(libs.bundles.test.engines)
    implementationDependencies.forEach { implementation(it) }

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(libs.bundles.junit)
    testImplementation(libs.mockito)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testCompileOnly(libs.jetbrains)

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.gson)

    integrationTestCompileOnly(libs.lombok)
    integrationTestAnnotationProcessor(libs.lombok)
    integrationTestCompileOnly(libs.jetbrains)

    integrationTestImplementation(libs.bundles.test.frameworks)
}

tasks.compileJava {
    // compile to the required Java version
    javaCompiler = javaToolchains.compilerFor { languageVersion = compileJavaVersion }
    compileJavaVersionInt.takeIf { it > 8 }?.let { options.release = it }
}

tasks.test {
    useJUnitPlatform()
    compileJavaVersionInt.takeIf { it > 21 }?.let { jvmArgs = listOf("-XX:+EnableDynamicAgentLoading") }
}

tasks.processResources {
    filesMatching("*.(yml|yaml|json)") {
        expand(
            mapOf(
                "group" to project.group,
                "version" to project.version,
                "name" to project.name
            )
        )
    }
}