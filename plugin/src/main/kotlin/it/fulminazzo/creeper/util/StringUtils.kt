package it.fulminazzo.creeper.util

import java.security.MessageDigest

/**
 * Hashes the current string in the SHA256 algorithm.
 *
 * @return the hash
 */
fun String.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
}