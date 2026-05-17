plugins {
    java
    groovy
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "${rootProject.group}.${rootProject.name}.runner.ServerRunner"
    }
}

afterEvaluate {
    val functionalTestImplementation: Configuration by configurations
    val functionalTestRuntimeOnly: Configuration by configurations

    dependencies {
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        compileOnly(libs.jetbrains)

        functionalTestImplementation(libs.spock)
        functionalTestRuntimeOnly(libs.junit.launcher)
    }

    tasks.withType<Test>().configureEach {
        testLogging {
            showStandardStreams = true
        }
    }

}
