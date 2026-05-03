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

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains)

    compileOnly(libs.gson)

    implementation(libs.junit.launcher)
    implementationDependencies.forEach { implementation(it) }

    testImplementation(libs.junit)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testCompileOnly(libs.jetbrains)

    testImplementation(libs.gson)
}

tasks.compileJava {
    // compile to the required Java version
    javaCompiler = javaToolchains.compilerFor { languageVersion = compileJavaVersion }
    options.release.set(compileJavaVersion.asInt())
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
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