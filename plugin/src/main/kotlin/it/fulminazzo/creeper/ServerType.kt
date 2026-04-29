package it.fulminazzo.creeper

/**
 * Identifies the type of server to run.
 */
sealed interface ServerType {
    /**
     * [ServerType] only for Minecraft server platforms.
     *
     * @property name the name of the platform
     * @constructor Create an empty Minecraft type
     */
    sealed class MinecraftType(val name: String) : ServerType

    data object VANILLA : MinecraftType("Vanilla")
    data object SPIGOT : MinecraftType("Spigot")
    data object BUKKIT : MinecraftType("Bukkit")
    data object PAPER : MinecraftType("Paper")
    data object PURPUR : MinecraftType("Purpur")
    data object FOLIA : MinecraftType("Folia")

}