plugins {
    java
    groovy
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jetbrains)
}

tasks.withType<Test>().configureEach {
    dependencies {
        implementation(libs.spock)
        runtimeOnly(libs.junit.launcher)
    }
}
