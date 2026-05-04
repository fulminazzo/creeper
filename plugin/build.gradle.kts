import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    jacoco

    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.buildconfig)

    id("creeper.test-configuration")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.jackson)

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    testImplementation(libs.mockk)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins.create("creeper") {
        id = "it.fulminazzo.creeper"
        implementationClass = "it.fulminazzo.creeper.CreeperPlugin"
    }
}

tasks.named<JavaCompile>("compileJava") {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
            "-Xannotation-default-target=param-property"
        )
    }
}

testConfiguration {
    testType("functional")
    testType("integration")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
}

tasks.jacocoTestReport {
    dependsOn(tasks.check)

    val testTasks = tasks.withType<Test>()
    executionData.setFrom(testTasks.map { testTask ->
        testTask.extensions.getByType<JacocoTaskExtension>().destinationFile
    })

    reports {
        xml.required = true
        csv.required = false
    }
}

configure<com.github.gmazzo.gradle.plugins.BuildConfigExtension> {
    val group = rootProject.group
    val name = rootProject.name

    packageName = "${group}.${name}"
    className = "ProjectInfo"

    buildConfigField("String", "GROUP", "\"${group}\"")
    buildConfigField("String", "NAME", "\"${name}\"")
    buildConfigField("String", "VERSION", "\"${rootProject.version}\"")
    buildConfigField("String", "USER_AGENT", "\"\$NAME/\$VERSION\"")
    buildConfigField(
        "String",
        "MOTD",
        "\"\\u00a72                 \$NAME\\u00a7r test server\\n\" + \n" +
                "\" Check out\\u00a7a https://github.com/fulminazzo/\$NAME\\u00a7f!\""
    )
}

afterEvaluate {

    tasks.named<Test>("functionalTest") {
        mustRunAfter("test", "integrationTest")
    }

}
