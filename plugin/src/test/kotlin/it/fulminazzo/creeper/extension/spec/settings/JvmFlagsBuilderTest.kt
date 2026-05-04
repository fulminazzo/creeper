package it.fulminazzo.creeper.extension.spec.settings

import it.fulminazzo.creeper.util.MemoryUnit
import it.fulminazzo.creeper.util.gb
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class JvmFlagsBuilderTest {
    private val project: Project = ProjectBuilder.builder().build()
    private val objects: ObjectFactory = project.objects

    private val builder = newBuilder()

    private fun newBuilder(): JvmFlagsBuilder = objects.newInstance(JvmFlagsBuilder::class.java)

    @Test
    fun `test that full build returns correct result`() {
        val expected = "-Xms1G -Xmx2G -XX:+UseG1GC " +
                "-XX:-ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 " +
                "-XX:G1NewSizePercent=30 -Daikars.new.flags=true"
        val builder = newBuilder()
        builder.minimumRam.set(1.gb)
        builder.xx("UseG1GC", true)
        builder.xx("ParallelRefProcEnabled", true)
        builder.xx("ParallelRefProcEnabled", false)
        builder.xx("MaxGCPauseMillis", 300)
        builder.xx("MaxGCPauseMillis", 200)
        builder.xx("G1NewSizePercent", 30)
        builder.property("aikars.new.flags", true)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @Test
    fun `test that only memory build returns correct result`() {
        val expected = "-Xms1K -Xmx3G"
        builder.minRam(1, MemoryUnit.KB)
        builder.maxRam(3, MemoryUnit.GB)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @Test
    fun `test that only developer boolean build returns correct result`() {
        val expected = "-Xms512M -Xmx2G -XX:+UseG1GC -XX:-ParallelRefProcEnabled"
        builder.xx("UseG1GC", true)
        builder.xx("ParallelRefProcEnabled", false)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @Test
    fun `test that only developer value build returns correct result`() {
        val expected = "-Xms512M -Xmx2G -XX:MaxGCPauseMillis=200 -XX:G1NewSizePercent=30"
        builder.xx("MaxGCPauseMillis", 200)
        builder.xx("G1NewSizePercent", 30)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @Test
    fun `test that only property build returns correct result`() {
        val expected = "-Xms512M -Xmx2G -Daikars.new.flags=true"
        builder.property("aikars.new.flags", true)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "-1, 2",
            "0, 2",
            "512, 0",
            "512, -1"
        ]
    )
    fun `test that negative or zero memory throws`(min: String, max: String) {
        assertThrows<GradleException> {
            builder.minRam(min.toInt(), MemoryUnit.MB)
            builder.maxRam(max.toInt(), MemoryUnit.MB)
            builder.build()
        }
    }

}