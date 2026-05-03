val integrationTestImplementation by configurations.getting {}

dependencies {
    compileOnly(libs.spigot)

    implementation(libs.slf4j.jdk)

    testImplementation(libs.spigot)
    testImplementation(libs.mockbukkit)
    integrationTestImplementation(libs.spigot)
    integrationTestImplementation(libs.mockbukkit)
}