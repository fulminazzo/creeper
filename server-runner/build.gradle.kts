plugins {
    java
    groovy
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
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

}
