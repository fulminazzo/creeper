package it.fulminazzo.creeper.extension.spec.settings

import org.gradle.api.GradleException

/**
 * Requires that the given value is a natural number (greater than or equal to 0).
 *
 * @param T the type of the value
 * @param value the value to check
 * @param name the name of the value
 * @return the value
 */
fun <T : Comparable<Int>> requireNatural(value: T, name: String): T =
    check(value, name, { v -> v >= 0 }, "Invalid %1\$s = %2\$s, must be >= 0")

/**
 * Requires that the given value is a positive number.
 *
 * @param T the type of the value
 * @param value the value to check
 * @param name the name of the value
 * @return the value
 */
fun <T : Comparable<Int>> requirePositive(value: T, name: String): T =
    check(value, name, { v -> v > 0 }, "Invalid %1\$s = %2\$s, must be > 0")

/**
 * Checks if the given value respects the predicate.
 *
 * @param T the type of the value
 * @param value the value to check
 * @param name the name of the value
 * @param predicate the predicate to check
 * @param message the error message in case of failed validation
 * @receiver the value to check
 * @return the value
 */
fun <T> check(value: T, name: String, predicate: (T) -> Boolean, message: String): T =
    if (predicate(value)) value else throw GradleException(message.format(name, value))