package it.fulminazzo.creeper.provider

import it.fulminazzo.creeper.ServerType

/**
 * Exception thrown when attempting to get a JAR build fails.
 *
 * @constructor Create a new Jar not found exception
 *
 * @param platform the requested platform
 * @param version the requested version
 */
class JarNotFoundException(platform: ServerType, version: String) :
    Exception("Could not find jar for platform=${platform.name} and version=$version")

/**
 * Exception thrown when attempting to get a configuration file fails.
 *
 * @constructor Create a new Configuration not found exception
 *
 * @param name the requested file name
 * @param platform the requested platform
 * @param version the requested version
 */
class ConfigurationNotFoundException(name: String, platform: ServerType, version: String) :
    Exception("Could not find configuration file $name for platform=${platform.name} and version=$version")

