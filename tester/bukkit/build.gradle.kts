val integrationTestImplementation by configurations.getting {}

dependencies {
    compileOnly(libs.spigot)

    implementation(libs.slf4j.jdk)

    testImplementation(libs.spigot)
    integrationTestImplementation(libs.spigot)
}