package it.fulminazzo.creeper

/**
 * Identifies an object that can be hashed.
 */
interface Hashable {

    /**
     * Converts this object to a hash string.
     *
     * @return the hash string
     */
    fun toHashString(): String

}