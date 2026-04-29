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
    fun minimumRam(value: Int, unit: MemoryUnit) {
        minimumRam(MemorySize(value, unit))
    }

    /**
     * Specifies the minimum memory allocation.
     *
     * @param size the size of the allocation
     */
    fun minimumRam(size: MemorySize) {
        minimumRam = size
    }

    /**
     * Specifies the maximum memory allocation.
     *
     * @param value the value of the allocation
     * @param unit the unit of the allocation
     */
    fun maximumRam(value: Int, unit: MemoryUnit) {
        maximumRam(MemorySize(value, unit))
    }

    /**
     * Specifies the maximum memory allocation.
     *
     * @param size the size of the allocation
     */
    fun maximumRam(size: MemorySize) {
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
     * Creates a new list of flags.
     *
     * @return the list of flags
     */
    fun build(): String {
        var flags = "-Xms${minimumRam.toJvmFlags()} -Xmx${maximumRam.toJvmFlags()}"
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

    }

}