plugins {
    java
}

dependencies {
    compileOnly(libs.spigot)

    implementation(project(":bukkit"))
    implementation(project(":integration-test"))
}