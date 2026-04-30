package it.fulminazzo.creeper.server.spec

/**
 * Identifies an exception occurred during the build process.
 *
 * @constructor Creates a new Build exception
 *
 * @param message the exception message
 */
class BuildException(message: String) : Exception(message)

/**
 * Requires that the given [Int] is between 1 and 65535.
 *
 * @param name the name of the value
 * @return the value
 * @throws BuildException if it fails
 */
fun Int.requirePort(name: String = "port"): Int = requireBetween(1, 65535, name)

/**
 * Requires that the given [Int] is between the given [min] and [max].
 *
 * @param min the minimum value
 * @param max the maximum value
 * @param name the name of the value
 * @return the value
 * @throws BuildException if it fails
 */
fun Int.requireBetween(min: Int, max: Int, name: String): Int =
    require({ it in min..max }, $$"Invalid $$name '%1$s': must be between $$min and $$max")

/**
 * Requires that the given [Int] is a natural number (greater than or equal to 0).
 *
 * @param name the name of the value
 * @return the value
 * @throws BuildException if it fails
 */
fun Int.requireNatural(name: String): Int =
    require({ it >= 0 }, $$"Invalid $$name '%1$s': must be at least 0")

/**
 * Requires that the given [Int] is a positive number.
 *
 * @param name the name of the value
 * @return the value
 * @throws BuildException if it fails
 */
fun Int.requirePositive(name: String): Int =
    require({ it > 0 }, $$"Invalid $$name '%1$s': must be positive")

/**
 * Requires that the given [Int] satisfies the [predicate].
 *
 * @param predicate the predicate to check
 * @param message the error message in case of failed validation
 * @receiver the value to check
 * @return the value
 * @throws BuildException if it fails
 */
fun Int.require(predicate: (Int) -> Boolean, message: String): Int =
    if (predicate(this)) this else throw BuildException(message.format(this))