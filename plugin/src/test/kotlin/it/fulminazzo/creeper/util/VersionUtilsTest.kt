package it.fulminazzo.creeper.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.Runtime.Version
import java.util.stream.Stream
import kotlin.test.Test

class VersionUtilsTest {

    @ParameterizedTest
    @MethodSource("provideTestVersions")
    fun `test that getJavaVersion returns correct version`(version: String, expectedJavaVersion: String) {
        assertEquals(Version.parse(expectedJavaVersion), VersionUtils.getJavaVersion(version))
    }

    @Test
    fun `test that getJavaVersion throws on invalid version`() {
        assertThrows<IllegalArgumentException> { VersionUtils.getJavaVersion("invalid") }
    }

    private companion object {

        @JvmStatic
        fun provideTestVersions(): Stream<Arguments> = Stream.of(
            // 1.0
            Arguments.of("1.0", "5"),
            Arguments.of("1.0.1", "5"),
            Arguments.of("1.0-SNAPSHOT", "5"),
            // 1.1
            Arguments.of("1.1", "5"),
            Arguments.of("1.1.1", "5"),
            Arguments.of("1.1-SNAPSHOT", "5"),
            // 1.2
            Arguments.of("1.2", "5"),
            Arguments.of("1.2.1", "5"),
            Arguments.of("1.2-SNAPSHOT", "5"),
            // 1.3
            Arguments.of("1.3", "5"),
            Arguments.of("1.3.1", "5"),
            Arguments.of("1.3-SNAPSHOT", "5"),
            // 1.4
            Arguments.of("1.4", "5"),
            Arguments.of("1.4.1", "5"),
            Arguments.of("1.4-SNAPSHOT", "5"),
            // 1.5
            Arguments.of("1.5", "5"),
            Arguments.of("1.5.1", "5"),
            Arguments.of("1.5-SNAPSHOT", "5"),
            Arguments.of("1.5.2", "5"),
            // 1.6
            Arguments.of("1.6", "6"),
            Arguments.of("1.6.1", "6"),
            Arguments.of("1.6-SNAPSHOT", "6"),
            Arguments.of("1.6.4", "6"),
            // 1.7
            Arguments.of("1.7", "6"),
            Arguments.of("1.7.1", "6"),
            Arguments.of("1.7-SNAPSHOT", "6"),
            Arguments.of("1.7.10", "6"),
            // 1.8
            Arguments.of("1.8", "6"),
            Arguments.of("1.8.1", "6"),
            Arguments.of("1.8-SNAPSHOT", "6"),
            Arguments.of("1.8.9", "6"),
            // 1.9
            Arguments.of("1.9", "6"),
            Arguments.of("1.9.1", "6"),
            Arguments.of("1.9-SNAPSHOT", "6"),
            Arguments.of("1.9.4", "6"),
            // 1.10
            Arguments.of("1.10", "6"),
            Arguments.of("1.10.1", "6"),
            Arguments.of("1.10-SNAPSHOT", "6"),
            Arguments.of("1.10.2", "6"),
            // 1.11
            Arguments.of("1.11", "6"),
            Arguments.of("1.11.1", "6"),
            Arguments.of("1.11-SNAPSHOT", "6"),
            Arguments.of("1.11.2", "6"),
            // 1.12
            Arguments.of("1.12", "8"),
            Arguments.of("1.12.1", "8"),
            Arguments.of("1.12-SNAPSHOT", "8"),
            Arguments.of("1.12.2", "8"),
            // 1.13
            Arguments.of("1.13", "8"),
            Arguments.of("1.13.1", "8"),
            Arguments.of("1.13-SNAPSHOT", "8"),
            Arguments.of("1.13.2", "8"),
            // 1.14
            Arguments.of("1.14", "8"),
            Arguments.of("1.14.1", "8"),
            Arguments.of("1.14-SNAPSHOT", "8"),
            Arguments.of("1.14.4", "8"),
            // 1.15
            Arguments.of("1.15", "8"),
            Arguments.of("1.15.1", "8"),
            Arguments.of("1.15-SNAPSHOT", "8"),
            Arguments.of("1.15.2", "8"),
            // 1.16
            Arguments.of("1.16", "8"),
            Arguments.of("1.16.1", "8"),
            Arguments.of("1.16-SNAPSHOT", "8"),
            Arguments.of("1.16.5", "8"),
            // 1.17
            Arguments.of("1.17", "16"),
            Arguments.of("1.17.1", "16"),
            Arguments.of("1.17-SNAPSHOT", "16"),
            // 1.18
            Arguments.of("1.18", "17"),
            Arguments.of("1.18.1", "17"),
            Arguments.of("1.18-SNAPSHOT", "17"),
            Arguments.of("1.18.2", "17"),
            // 1.19
            Arguments.of("1.19", "17"),
            Arguments.of("1.19.1", "17"),
            Arguments.of("1.19-SNAPSHOT", "17"),
            Arguments.of("1.19.4", "17"),
            // 1.20
            Arguments.of("1.20", "17"),
            Arguments.of("1.20.1", "17"),
            Arguments.of("1.20-SNAPSHOT", "17"),
            Arguments.of("1.20.4", "17"),
            // 1.20.5
            Arguments.of("1.20.5", "21"),
            Arguments.of("1.20.6", "21"),
            Arguments.of("1.20.5-SNAPSHOT", "21"),
            Arguments.of("1.20.6-SNAPSHOT", "21"),
            // 1.21
            Arguments.of("1.21", "21"),
            Arguments.of("1.21.1", "21"),
            Arguments.of("1.21-SNAPSHOT", "21"),
            Arguments.of("1.21.11", "21"),
            // 26.1
            Arguments.of("26.1", "25"),
            Arguments.of("26.1.1", "25"),
            Arguments.of("26.1-SNAPSHOT", "25"),
        )

    }

}