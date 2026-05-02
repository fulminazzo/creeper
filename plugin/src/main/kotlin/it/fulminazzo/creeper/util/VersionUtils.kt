package it.fulminazzo.creeper.util

import java.lang.Runtime.Version

/**
 * A collection of utilities for Minecraft versions.
 */
object VersionUtils {
    private val MINECRAFT_VERSION_PATTERN = "^([0-9]+)\\.([0-9]+)(?:\\.([0-9]+))?(?:[.\\-].*)?$".toRegex()

    /**
     * Gets the minimum required Java version for the specified Minecraft version.
     * Check https://minecraft.wiki/w/Tutorial:Update_Java#Choosing_a_Java for more.
     *
     * @param version the Minecraft version
     */
    fun getJavaVersion(version: String): Version {
        val match = MINECRAFT_VERSION_PATTERN.matchEntire(version)?.groupValues
        return match?.let { m ->
            val major = m[1].toInt()
            val minor = m[2].toInt()
            Version.parse(
                if (major > 1) "25"
                else when (minor) {
                    in 0..5 -> "5"
                    in 6..11 -> "6"
                    in 12..16 -> "8"
                    17 -> "16"
                    in 18..19 -> "17"
                    20 -> match[3].takeIf { it.isNotEmpty() }?.toInt()
                        ?.let { if (it >= 5) "21" else "17" }
                        ?: "17"
                    else -> "21"
                }
            )
        } ?: throw IllegalArgumentException("Invalid Minecraft version: $version")
    }

}