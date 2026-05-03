import com.github.jengelman.gradle.plugins.shadow.transformers.GroovyExtensionModuleTransformer

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
//        "org.apache.groovy" to "apache.groovy", // not relocating for issues
//        "org.codehaus.groovy" to "codehaus.groovy", // not relocating for issues
//        "groovy" to "groovy", // not relocating for issues
//        "groovyjarjarantlr4" to "groovyjarjarantlr4", // not relocating for issues
//        "groovyjarjarasm" to "groovyjarjarasm", // not relocating for issues
//        "groovyjarjarpicocli" to "groovyjarjarpicocli", // not relocating for issues
        "spock" to "spock",
        "org.spockframework" to "spockframework",
//        "munit" to "munit", // not relocating for issues
        "io.leangen.geantyref" to "geantyref",
        "javax" to "javax",
        "org.slf4j" to "slf4j"
    ).forEach { (from, to) -> relocate(from, "$basePackage.$to") }

    dependencies {
        exclude("dsld/**", "sbt/**", "org/scalatools/**")
    }

    mergeServiceFiles()
    transform(GroovyExtensionModuleTransformer::class.java)
}