plugins {
    java
    groovy
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<Test>().configureEach {
    dependencies {
        implementation(libs.spock)
        runtimeOnly(libs.junit.launcher)
    }
}
