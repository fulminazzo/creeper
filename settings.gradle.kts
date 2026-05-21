@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven {
            name = "spigotmc-repo"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
    }

}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "creeper"

include(
    "server-runner",

    "plugin"
)

include(
    "test-runner",
    "test-runner:base",
    "test-runner:bukkit"
)
