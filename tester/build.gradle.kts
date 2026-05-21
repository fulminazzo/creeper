plugins {
    java

    alias(libs.plugins.shadow)
}

val projectName = "${
    rootProject.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}${
    project.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}"

allprojects {
    val libs = rootProject.libs
    val baseProject = project(":tester:base")

    apply { plugin("java") }
    apply { plugin(libs.plugins.shadow.get().pluginId) }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    afterEvaluate {
        val integrationTestCompileOnly by configurations.getting {}
        val integrationTestAnnotationProcessor by configurations.getting {}

        dependencies {
            compileOnly(libs.lombok)
            annotationProcessor(libs.lombok)
            compileOnly(libs.jetbrains)

            if (project.path != baseProject.path) implementation(baseProject)

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
    }

    tasks.jar {
        archiveClassifier = "original"
        archiveBaseName = if (project.name == baseProject.name) project.name else projectName
    }

    tasks.shadowJar {
        archiveClassifier = ""
        archiveBaseName = if (project.name == baseProject.name) project.name else projectName

        dependencies {
            val jetbrainsAnnotations = libs.jetbrains.get().module
            exclude(dependency(jetbrainsAnnotations.group + ":" + jetbrainsAnnotations.name))
            exclude(dependency("org.apache-extras.beanshell:bsh"))
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("net.java.dev.jna:jna-platform"))

            exclude(
                "DebugProbesKt.bin", // from kotlin
                "testng.css", "testng-1.0.dtd", "testngtasks", // from testng
                "win32-x86/**", "win32-x86-64/**", // from jna
                "html/**", "images/**", "scala-*.properties", "library.properties", "rootdoc.txt", // from scalatest
                "**/COPYRIGHT*", "**/LICENSE*", "**/NOTICE*"
            )

            listOf(
                "com.android.tools/**", "licenses/**", "maven/**", "proguard/**", "*.version", "*.kotlin_module"
            ).forEach { exclude("META-INF/$it") }
        }

        mergeServiceFiles()
    }

    tasks.build {
        dependsOn(tasks.shadowJar)
    }

    tasks.processResources {
        val module = project(":tester")
        val group = module.group
        val rootProjectName = rootProject.name
        val projectName = module.name
        val version = module.version
        val commandName = "run${rootProjectName}tests"
        val pluginName = "${
            rootProjectName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }${
            projectName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }"
        filesMatching(listOf("*.yml")) {
            expand(
                mapOf(
                    "group" to "${group}.${rootProjectName}",
                    "version" to version,
                    "name" to pluginName,
                    "name_lower" to pluginName.lowercase(),
                    "command_name" to commandName,
                    "command_description" to "Runs all the tests contained in the plugin. " +
                            "WARNING: to ensure maximum compatibility, these tests will be run synchronously " +
                            "when possible. Be ready to lag spikes and other undesirable effects.",
                    "command_usage" to "/$commandName",
                    "command_aliases" to emptyList<String>()
                )
            )
        }
    }

}

dependencies {
    subprojects.filter { !it.name.contains("test") }.forEach { implementation(it) }
}

tasks.check {
    dependsOn(subprojects.map { it.tasks.check })
}
