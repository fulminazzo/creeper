plugins {
    jacoco

    id("creeper.test-configuration")
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

allprojects {
    apply { plugin("jacoco") }

    apply { plugin("creeper.test-configuration") }

    repositories {
        mavenCentral()
    }

    testConfiguration {
        testType("functional")
        testType("integration")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
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

    afterEvaluate {

        tasks.named<Test>("functionalTest") {
            mustRunAfter("test", "integrationTest")
        }

    }

}