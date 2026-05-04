package it.fulminazzo.creeper.util

/**
 * Identifies the memory unit when specifying RAM allocation size.
 *
 * @property jvmUnit the unit in the JVM flags format
 * @constructor Create a new Memory unit
 */
enum class MemoryUnit(val jvmUnit: String) {
    /**
     * Byte memory unit.
     */
    B(""),

    /**
     * Kilobyte memory unit.
     */
    KB("K"),

    /**
     * Megabyte memory unit.
     */
    MB("M"),

    /**
     * Gigabyte memory unit.
     */
    GB("G");

    companion object {

        /**
         * Gets the [MemoryUnit] by its unit.
         * The unit supports both JVM flags (`G`) and the human-readable format (`GB`).
         *
         * @param unit the raw unit
         * @return the unit (if found)
         */
        fun ofUnit(unit: String): MemoryUnit? =
            entries.find {
                it.jvmUnit.equals(
                    if (unit.uppercase().endsWith("B")) unit.dropLast(1) else unit,
                    ignoreCase = true
                )
            }

    }

}

/**
 * Defines a memory size.
 *
 * @property value the value
 * @property unit the unit of the value
 * @constructor Create a new Memory size
 */
data class MemorySize(val value: Int, val unit: MemoryUnit) : Comparable<Int> {

    override operator fun compareTo(other: Int): Int = value.compareTo(other)

    operator fun unaryMinus() = MemorySize(-value, unit)

    override fun toString(): String = "${value}${unit.jvmUnit}"

}

val Int.b: MemorySize get() = MemorySize(this, MemoryUnit.B)
val Int.kb: MemorySize get() = MemorySize(this, MemoryUnit.KB)
val Int.mb: MemorySize get() = MemorySize(this, MemoryUnit.MB)
val Int.gb: MemorySize get() = MemorySize(this, MemoryUnit.GB)
