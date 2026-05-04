plugins {
    `java-gradle-plugin`
    jacoco

    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.buildconfig)

    id("creeper.test-configuration")
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

configure<com.github.gmazzo.buildconfig.BuildConfigExtension> {
    val group = rootProject.group
    val name = rootProject.name

    packageName = "${group}.${name}"
    className = "ProjectInfo"

    buildConfigField("String", "GROUP", "\"${group}\"")
    buildConfigField("String", "NAME", "\"${name}\"")
    buildConfigField("String", "VERSION", "\"${rootProject.version}\"")
    buildConfigField("String", "USER_AGENT", $$"\"$NAME/$VERSION\"")
}

afterEvaluate {

    tasks.named<Test>("functionalTest") {
        mustRunAfter("test", "integrationTest")
    }

}
