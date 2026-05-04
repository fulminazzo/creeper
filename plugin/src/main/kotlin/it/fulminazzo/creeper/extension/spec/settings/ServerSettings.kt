package it.fulminazzo.creeper.extension.spec.settings

import it.fulminazzo.creeper.util.MemorySize
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import java.io.Serializable

/**
 * Holds all the settings for a general server to run.
 *
 * @property port the port the server should run on
 * @property players the maximum number of players allowed
 * @property flags the JVM flags to use
 * @constructor Creates a new Server config
 */
sealed class ServerSettings(val port: Int, val players: Int, val flags: String) : Serializable

/**
 * Builder for [ServerSettings].
 *
 * @constructor Creates a new Server settings builder
 */
abstract class ServerSettingsBuilder : RamConfigurator() {
    abstract val port: Property<Int>
    abstract val maximumPlayers: Property<Int>

    @get:Nested
    abstract val flags: JvmFlagsBuilder

    override val minimumRam: Property<MemorySize> get() = flags.minimumRam
    override val maximumRam: Property<MemorySize> get() = flags.maximumRam

    /**
     * PROPERTY VALUES GETTERS
     */
    protected val portValue: Int
        get() {
            val p = port.getOrElse(25565)
            return p.takeIf { it in 1..65535 }
                ?: throw GradleException("Invalid port = $p, must be between 1 and 65535")
        }
    protected val maximumPlayersValue: Int
        get() = requirePositive(maximumPlayers.getOrElse(20), "maximumPlayers")

    protected val flagsValue: String get() = flags.build()

    /**
     * Builds the server settings.
     *
     * @return the server settings
     * @throws GradleException if the configuration is invalid
     */
    abstract fun build(): ServerSettings

    /**
     * Applies the configuration to the JVM flags builder.
     *
     * @param action the configuration
     */
    fun flags(action: Action<JvmFlagsBuilder>) = action.execute(flags)

    companion object {

        /**
         * Helper function to extract an enum value from the given property.
         *
         * @param E the enum type
         * @param enumClass the enum class
         * @param name the name of the property
         * @param property the property
         * @return the enum value
         */
        @JvmStatic
        protected fun <E : Enum<E>> getEnumValue(enumClass: Class<E>, name: String, property: Property<String>): E {
            val raw = property.orNull ?: throw GradleException("Invalid server configuration, missing: $name =")
            return enumClass.enumConstants.find { it.name.equals(raw, ignoreCase = true) }
                ?: throw GradleException("Invalid server configuration, unrecognized: $name = $raw")
        }

    }

}