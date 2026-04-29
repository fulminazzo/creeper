package it.fulminazzo.creeper.config

/**
 * Builder for JVM flags.
 */
class JvmFlagsBuilder {
    var minimumRam = 512.mb
    var maximumRam = 2.gb
    private val enabledDeveloperFlags = mutableMapOf<String, Boolean>()
    private val valueDeveloperFlags = mutableMapOf<String, String>()
    private val propertyFlags = mutableMapOf<String, String>()

    /*
     * RAM
     */

    /**
     * Specifies the minimum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     */
    fun setMinimumRam(value: Int, unit: MemoryUnit) {
        setMinimumRam(MemorySize(value, unit))
    }

    /**
     * Specifies the minimum memory allocation.
     *
     * @param size the size of the allocation
     */
    fun setMinimumRam(size: MemorySize) {
        minimumRam = size
    }

    /**
     * Specifies the maximum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     */
    fun setMaximumRam(value: Int, unit: MemoryUnit) {
        setMaximumRam(MemorySize(value, unit))
    }

    /**
     * Specifies the maximum memory allocation.
     *
     * @param size the size of the allocation
     */
    fun setMaximumRam(size: MemorySize) {
        maximumRam = size
    }

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
     * Applies the flags of another [JvmFlagsBuilder] to the current one.
     *
     * Does not override existing flags.
     *
     * @param other the other flags builder
     */
    fun from(other: JvmFlagsBuilder) = apply {
        minimumRam = other.minimumRam
        maximumRam = other.maximumRam
        enabledDeveloperFlags.putAll(other.enabledDeveloperFlags)
        valueDeveloperFlags.putAll(other.valueDeveloperFlags)
        propertyFlags.putAll(other.propertyFlags)
    }

    /**
     * Creates a new list of flags.
     *
     * @return the list of flags
     */
    fun build(): String {
        var flags = "-Xms$minimumRam -Xmx$maximumRam"
        flags += buildEnabledDeveloperFlags().takeIf { it.isNotEmpty() }?.let { " $it" } ?: ""
        flags += buildValueDeveloperFlags().takeIf { it.isNotEmpty() }?.let { " $it" } ?: ""
        flags += buildPropertyFlags().takeIf { it.isNotEmpty() }?.let { " $it" } ?: ""
        return flags
    }

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

        /**
         * Akair's flags
         */
        val AKAIR_FLAGS = JvmFlagsBuilder().apply {
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

    }

}