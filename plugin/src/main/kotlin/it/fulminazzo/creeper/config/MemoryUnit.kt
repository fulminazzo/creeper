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

/**
 * Defines a memory size.
 *
 * @property value the value
 * @property unit the unit of the value
 * @constructor Create a new Memory size
 */
data class MemorySize(val value: Long, val unit: MemoryUnit) {

    /**
     * Converts the current size in the JVM flags format.
     *
     * @return the formatted size
     */
    fun toJvmFlags(): String = "${value}${unit.jvmUnit}"

}

val Int.b: MemorySize get() = MemorySize(toLong(), MemoryUnit.B)
val Int.kb: MemorySize get() = MemorySize(toLong(), MemoryUnit.KB)
val Int.mb: MemorySize get() = MemorySize(toLong(), MemoryUnit.MB)
val Int.gb: MemorySize get() = MemorySize(toLong(), MemoryUnit.GB)

val Long.b: MemorySize get() = MemorySize(toLong(), MemoryUnit.B)
val Long.kb: MemorySize get() = MemorySize(toLong(), MemoryUnit.KB)
val Long.mb: MemorySize get() = MemorySize(toLong(), MemoryUnit.MB)
val Long.gb: MemorySize get() = MemorySize(toLong(), MemoryUnit.GB)
