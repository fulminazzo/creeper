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

    data object VANILLA : MinecraftType("VANILLA")
    data object SPIGOT : MinecraftType("SPIGOT")
    data object BUKKIT : MinecraftType("BUKKIT")
    data object PAPER : MinecraftType("PAPER")
    data object FOLIA : MinecraftType("FOLIA")

}