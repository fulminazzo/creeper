package it.fulminazzo.creeper.config

/**
 * Identifies the memory unit when specifying RAM allocation size.
 *
 * @property jvmUnit the unit in the JVM flags format
 * @constructor Create a new Memory unit
 */
sealed class MemoryUnit(val jvmUnit: String) {

    /**
     * Byte memory unit.
     */
    data object B : MemoryUnit("")

    /**
     * Kilobyte memory unit.
     */
    data object KB : MemoryUnit("K")

    /**
     * Megabyte memory unit.
     */
    data object MB : MemoryUnit("M")

    /**
     * Gigabyte memory unit.
     */
    data object GB : MemoryUnit("G")

}