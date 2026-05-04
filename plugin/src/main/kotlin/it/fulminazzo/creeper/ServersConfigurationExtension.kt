package it.fulminazzo.creeper

import it.fulminazzo.creeper.server.spec.BuildException
import it.fulminazzo.creeper.server.spec.MinecraftServerSpecBuilder
import it.fulminazzo.creeper.server.spec.ServerSpec
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Extension for configuring servers to run.
 *
 * @constructor Creates a new Servers configuration extension
 *
 * @param objects the factory to get a list property from
 */
abstract class ServersConfigurationExtension @Inject constructor(objects: ObjectFactory) {

    internal val specifications = objects.listProperty(ServerSpec::class.java)

    /**
     * Adds a new [it.fulminazzo.creeper.server.spec.MinecraftServerSpec] to the list of servers to run.
     *
     * @param configuration the configuration of the server to add
     * @receiver the server configuration builder
     */
    fun minecraftServer(configuration: MinecraftServerSpecBuilder.() -> Unit) {
        val builder = MinecraftServerSpecBuilder()
        configuration(builder)
        try {
            specifications.add(builder.build())
        } catch (e: BuildException) {
            throw GradleException(e.message!!)
        }
    }

}