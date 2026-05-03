val integrationTestImplementation by configurations.getting {}

dependencies {
    compileOnly(libs.spigot)

    testImplementation(libs.spigot)
    integrationTestImplementation(libs.spigot)
}