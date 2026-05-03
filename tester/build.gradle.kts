plugins {
    java
}

// VARIABLES START
val compileJavaVersion = JavaLanguageVersion.of(8)
val gradleJavaVersion = JavaLanguageVersion.of(8)
val implementationDependencies = listOf<String>()
// VARIABLES END

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(gradleJavaVersion)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementationDependencies.forEach { implementation(it) }
}

tasks.compileJava {
    // compile to the required Java version
    javaCompiler = javaToolchains.compilerFor { languageVersion = compileJavaVersion }
    options.release.set(compileJavaVersion.asInt())
}

tasks.test {
    useJUnitPlatform()
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