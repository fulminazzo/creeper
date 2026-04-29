package it.fulminazzo.creeper.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JvmFlagsBuilderTest {

    @Test
    fun `test that full build returns correct result`() {
        val expected = "-Xms1G -Xmx2G -XX:+UseG1GC -XX:-ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:G1NewSizePercent=30 -Daikars.new.flags=true"
        val builder = JvmFlagsBuilder()
        builder.minimumRam = 1.gb
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
        val builder = JvmFlagsBuilder()
        builder.minimumRam(1, MemoryUnit.KB)
        builder.maximumRam(3, MemoryUnit.GB)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @Test
    fun `test that only developer boolean build returns correct result`() {
        val expected = "-Xms512M -Xmx2G -XX:+UseG1GC -XX:-ParallelRefProcEnabled"
        val builder = JvmFlagsBuilder()
        builder.xx("UseG1GC", true)
        builder.xx("ParallelRefProcEnabled", false)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @Test
    fun `test that only developer value build returns correct result`() {
        val expected = "-Xms512M -Xmx2G -XX:MaxGCPauseMillis=200 -XX:G1NewSizePercent=30"
        val builder = JvmFlagsBuilder()
        builder.xx("MaxGCPauseMillis", 200)
        builder.xx("G1NewSizePercent", 30)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

    @Test
    fun `test that only property build returns correct result`() {
        val expected = "-Xms512M -Xmx2G -Daikars.new.flags=true"
        val builder = JvmFlagsBuilder()
        builder.property("aikars.new.flags", true)
        assertEquals(expected, builder.build(), "jvm flags were not equal")
    }

}