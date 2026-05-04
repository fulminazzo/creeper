package it.fulminazzo.creeper.extension.spec.settings

import it.fulminazzo.creeper.util.MemorySize
import it.fulminazzo.creeper.util.MemoryUnit
import org.gradle.api.GradleException
import org.gradle.api.provider.Property

/**
 * Marker class to denote an object capable of configuring RAM settings.
 */
abstract class RamConfigurator {
    abstract val minimumRam: Property<MemorySize>
    abstract val maximumRam: Property<MemorySize>

    /**
     * PROPERTY VALUES GETTERS
     */
    protected val minimumRamValue: MemorySize
        get() = requirePositive(minimumRam.get(), "minimumRam")
    protected val maximumRamValue: MemorySize
        get() = requirePositive(maximumRam.get(), "maximumRam")

    /**
     * Specifies the minimum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     * @throws org.gradle.api.GradleException if the unit is invalid
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
     * @throws org.gradle.api.GradleException if the unit is invalid
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

}