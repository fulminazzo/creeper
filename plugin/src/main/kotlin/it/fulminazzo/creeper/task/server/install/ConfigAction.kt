package it.fulminazzo.creeper.task.server.install

import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.extension.spec.settings.MinecraftServerSettings

/**
 * Represents an action to be performed on the server configuration file.
 */
sealed class ConfigAction {

    /**
     * Applies this action to the given configuration.
     *
     * @param configuration the configuration
     * @param specification the specification that requested the configuration
     */
    abstract fun apply(configuration: MutableMap<String, Any>, specification: ServerSpec<*, *>)

    /**
     * Applies all the specified settings in a [MinecraftServerSettings] to the `server.properties` file.
     */
    data object ServerProperties : ConfigAction() {

        override fun apply(configuration: MutableMap<String, Any>, specification: ServerSpec<*, *>) {
            val settings = specification.settings
            if (settings is MinecraftServerSettings) {
                configuration["hardcore"] = settings.hardcore
                configuration["server-port"] = settings.port
                configuration["max-players"] = settings.players
                configuration["difficulty"] = settings.difficulty.name.lowercase()
                configuration["gamemode"] = settings.gamemode.name.lowercase()
                configuration["generate-structures"] = settings.generateStructures
                configuration["online-mode"] = settings.onlineMode
                configuration["spawn-protection"] = settings.spawnProtection
                configuration["view-distance"] = settings.viewDistance
                configuration["simulation-distance"] = settings.simulationDistance
                configuration["white-list"] = settings.whitelist

                configuration["allow-nether"] = false
            }
        }

    }

    /**
     * Applies optimization settings to the `bukkit.yml` file.
     */
    data object BukkitConfig : ConfigAction() {

        override fun apply(configuration: MutableMap<String, Any>, specification: ServerSpec<*, *>) {
            val settings = configuration.getOrPut("settings") { mutableMapOf<String, Any>() }
            @Suppress("UNCHECKED_CAST")
            (settings as MutableMap<String, Any>)["allow-end"] = false
        }

    }

}