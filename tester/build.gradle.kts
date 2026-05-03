plugins {
    java
}

// VARIABLES START
val compileJavaVersion = JavaLanguageVersion.of(8)
val gradleJavaVersion = JavaLanguageVersion.of(8)
val implementationDependencies = listOf<String>()
val creeperGroup = "it.fulminazzo.creeper"
val creeperVersion = "0.0.1-SNAPSHOT"
// VARIABLES END

val compileJavaVersionInt = compileJavaVersion.asInt()

extra["implementationDependencies"] = implementationDependencies

group = creeperGroup
version = creeperVersion

allprojects {
    apply { plugin("java") }

    val libs = rootProject.libs

    java {
        toolchain {
            languageVersion.set(gradleJavaVersion)
        }
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

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        testImplementation(libs.junit.launcher)
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
                    "name" to project.name,
                    "commandName" to "creepertest",
                    "commandAliases" to listOf("creepert", "ctest", "ct"),
                    "commandDescription" to "Runs all the tests contained in the plugin. " +
                            "WARNING: to ensure maximum compatibility, these tests will be run synchronously " +
                            "when possible. Be ready to lag spikes and other undesirable effects."
                )
            )
        }
    }

}

tasks.check {
    dependsOn(subprojects.map { it.tasks.check })
}