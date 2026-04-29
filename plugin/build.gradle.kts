plugins {
    `java-gradle-plugin`
    jacoco

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildconfig)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.mockk)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins.create("creeper") {
        id = "it.fulminazzo.creeper"
        implementationClass = "it.fulminazzo.creeper.CreeperPlugin"
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
}

tasks.jacocoTestReport {
    dependsOn(tasks.check)

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
}

/**
 * FUNCTIONAL TESTS CONFIGURATION
 */
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    description = "Runs the functional test suite."
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}
