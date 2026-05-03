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

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(libs.junit)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testCompileOnly(libs.jetbrains)

    testImplementation(libs.gson)
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