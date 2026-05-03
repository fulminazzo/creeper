plugins {
    java
}

dependencies {
    compileOnly(libs.spigot)

    implementation(project(":bukkit"))
    implementation(project(":integration-test"))
}

tasks.shadowJar {
    val basePackage = "${rootProject.group}.${rootProject.name}.testlibs"
    mapOf(
        "org.apache.groovy" to "apache.groovy",
        "org.codehaus.groovy" to "codehaus.groovy",
        "groovy" to "groovy",
        "groovyjarjarantlr4" to "groovyjarjarantlr4",
        "groovyjarjarasm" to "groovyjarjarasm",
        "groovyjarjarpicocli" to "groovyjarjarpicocli",
        "spock" to "spock",
        "org.spockframework" to "spockframework",
        "munit" to "munit",
        "io.leangen.geantyref" to "geantyref",
        "javax" to "javax",
        "org.slf4j" to "slf4j"
    ).forEach { (from, to) -> relocate(from, "$basePackage.$to") }

    dependencies {
        exclude("dsld/**", "sbt/**", "org/scalatools/**")

        listOf("groovy-release-info.properties", "dgminfo", "resources/**").forEach { exclude("META-INF/$it") }
    }

    mergeServiceFiles()
}