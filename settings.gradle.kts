@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }

}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "creeper"

include(
    "server-runner",

    "plugin"
)
