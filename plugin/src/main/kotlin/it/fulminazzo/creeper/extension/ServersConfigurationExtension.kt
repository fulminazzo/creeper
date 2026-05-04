package it.fulminazzo.creeper.extension

import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpecBuilder
import it.fulminazzo.creeper.extension.spec.ServerSpec
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Extension for configuring servers to run.
 *
 * @constructor Creates a new Servers configuration extension
 *
 * @param objects the factory to generate objects from
 */
abstract class ServersConfigurationExtension @Inject internal constructor(private val objects: ObjectFactory) {

    internal val specifications = mutableListOf<ServerSpec<*, *>>()

    /**
     * Adds a new [MinecraftServerSpec] to the list of servers to run.
     *
     * @param action the configuration of the server to add
     */
    fun minecraftServer(action: Action<MinecraftServerSpecBuilder>) {
        val builder = objects.newInstance(MinecraftServerSpecBuilder::class.java)
        action.execute(builder)
        specifications.add(builder.build())
    }

}