package it.fulminazzo.creeper.extension.spec.settings

import it.fulminazzo.creeper.util.MemorySize
import it.fulminazzo.creeper.util.MemoryUnit
import it.fulminazzo.creeper.util.gb
import it.fulminazzo.creeper.util.mb
import org.gradle.api.GradleException
import org.gradle.api.provider.Property

/**
 * Builder for JVM flags.
 */
abstract class JvmFlagsBuilder {

    abstract val minimumRam: Property<MemorySize>

    abstract val maximumRam: Property<MemorySize>

    private val enabledDeveloperFlags = mutableMapOf<String, Boolean>()
    private val valueDeveloperFlags = mutableMapOf<String, String>()
    private val propertyFlags = mutableMapOf<String, String>()

    init {
        minimumRam.convention(512.mb)
        maximumRam.convention(2.gb)
    }

    /*
     * RAM
     */

    /**
     * Specifies the minimum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     */
    fun minRam(value: Int, unit: String) = minRam(
        value,
        MemoryUnit.entries.find { it.jvmUnit == unit } ?: throw GradleException("Invalid memory unit: $unit")
    )

    /**
     * Specifies the minimum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     */
    fun minRam(value: Int, unit: MemoryUnit) = minRam(MemorySize(value, unit))

    /**
     * Specifies the minimum memory allocation.
     *
     * @param size the size of the allocation
     */
    fun minRam(size: MemorySize) = minimumRam.set(size)

    /**
     * Specifies the maximum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     */
    fun maxRam(value: Int, unit: String) = maxRam(
        value,
        MemoryUnit.entries.find { it.jvmUnit == unit } ?: throw GradleException("Invalid memory unit: $unit")
    )

    /**
     * Specifies the maximum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     */
    fun maxRam(value: Int, unit: MemoryUnit) = maxRam(MemorySize(value, unit))

    /**
     * Specifies the maximum memory allocation.
     *
     * @param size the size of the allocation
     */
    fun maxRam(size: MemorySize) = maximumRam.set(size)

    /*
     * DEVELOPER FLAGS
     */

    /**
     * Specifies a flag of the form `-XX:+<something>` or `-XX:-<something>`.
     *
     * @param value the value
     * @param enable if `true`, the flag is enabled; otherwise, it is disabled
     */
    fun xx(value: String, enable: Boolean) {
        enabledDeveloperFlags[value] = enable
    }

    /**
     * Specifies a flag of the form `-XX:<key>=<value>`.
     *
     * @param name the name of the flag
     * @param value the value of the flag
     */
    fun xx(name: String, value: Any) {
        valueDeveloperFlags[name] = value.toString()
    }

    /*
     * PROPERTY FLAGS
     */

    /**
     * Specifies a flag of the form `-D<key>=<value>`.
     *
     * @param key the name of the flag
     * @param value the value of the flag
     */
    fun property(key: String, value: Any) {
        propertyFlags[key] = value.toString()
    }

    /**
     * PRESETS
     */
    fun aikars() {
        xx("UseG1GC", true)
        xx("ParallelRefProcEnabled", true)
        xx("MaxGCPauseMillis", 200)
        xx("UnlockExperimentalVMOptions", true)
        xx("DisableExplicitGC", true)
        xx("AlwaysPreTouch", true)
        xx("G1NewSizePercent", 30)
        xx("G1MaxNewSizePercent", 40)
        xx("G1HeapRegionSize", 8.mb)
        xx("G1ReservePercent", 20)
        xx("G1HeapWastePercent", 5)
        xx("G1MixedGCCountTarget", 4)
        xx("InitiatingHeapOccupancyPercent", 15)
        xx("G1MixedGCLiveThresholdPercent", 90)
        xx("G1RSetUpdatingPauseTimePercent", 5)
        xx("SurvivorRatio", 32)
        xx("PerfDisableSharedMem", true)
        xx("MaxTenuringThreshold", 1)
    }

    /**
     * Creates a new list of flags.
     *
     * @return the list of flags
     * @throws GradleException if an invalid value has been specified
     */
    fun build(): String {
        var flags = "-Xms${getMinimumRam()} -Xmx${getMaximumRam()}"
        flags += buildEnabledDeveloperFlags().takeIf { it.isNotEmpty() }?.let { " $it" } ?: ""
        flags += buildValueDeveloperFlags().takeIf { it.isNotEmpty() }?.let { " $it" } ?: ""
        flags += buildPropertyFlags().takeIf { it.isNotEmpty() }?.let { " $it" } ?: ""
        return flags
    }

    private fun getMinimumRam(): MemorySize = requirePositive(minimumRam.get(), "minimumRam")

    private fun getMaximumRam(): MemorySize = requirePositive(maximumRam.get(), "maximumRam")

    private fun buildEnabledDeveloperFlags(): String = enabledDeveloperFlags
        .map { (flag, enable) -> "$DEVELOPER_FLAG_PREFIX${if (enable) "+" else "-"}$flag" }
        .joinToString(" ")

    private fun buildValueDeveloperFlags(): String = valueDeveloperFlags
        .map { (flag, value) -> "$DEVELOPER_FLAG_PREFIX$flag=$value" }
        .joinToString(" ")

    private fun buildPropertyFlags(): String = propertyFlags
        .map { (flag, value) -> "$PROPERTY_FLAG_PREFIX$flag=$value" }
        .joinToString(" ")

    companion object {
        private const val DEVELOPER_FLAG_PREFIX = "-XX:"
        private const val PROPERTY_FLAG_PREFIX = "-D"

    }

}