package it.fulminazzo.creeper

import java.io.Serializable

/**
 * Identifies the type of server platform.
 *
 * @property name the name of the platform
 * @property parent the platform it was forked from (`null` if vanilla)
 * @constructor Creates a new Server type
 */
sealed class ServerType(val name: String, private val parent: ServerType?) : Serializable {
    val id: String = name.lowercase()

    /**
     * Checks if the server type is a fork of the specified server type.
     *
     * @param other the server type to check against
     * @return `true` if the server was created from the other server type
     */
    fun isForkOf(other: ServerType): Boolean = other == this || parent?.isForkOf(other) ?: false

    /**
     * [ServerType] only for Minecraft server platforms.
     *
     * @property name the name of the platform
     * @property parent the platform it was forked from (`null` if vanilla)
     * @constructor Create an empty Minecraft type
     */
    sealed class MinecraftType(name: String, parent: ServerType?) : ServerType(name, parent)

    data object VANILLA : MinecraftType("Vanilla", null)
    data object BUKKIT : MinecraftType("Bukkit", VANILLA)
    data object SPIGOT : MinecraftType("Spigot", BUKKIT)
    data object PAPER : MinecraftType("Paper", SPIGOT)
    data object PURPUR : MinecraftType("Purpur", PAPER)
    data object FOLIA : MinecraftType("Folia", PAPER)

    companion object {

        /**
         * Gets the [ServerType] by its name.
         */
        fun valueOf(name: String): ServerType? = values().find { it.name.equals(name, ignoreCase = true) }

        /**
         * Gets all the set values.
         *
         * @return the list of values
         */
        fun values(): List<ServerType> = listOf(VANILLA, BUKKIT, SPIGOT, PAPER, PURPUR, FOLIA)

    }

}