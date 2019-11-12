/*
 * Copyright The Titan Project Contributors.
 */
package io.titandata.remote

import java.net.URI

/**
 * The remote client interface defines functionality that runs in the context of the titan. It is responsible for
 * parsing the URI format of remotes, validating remote properties, and createing remote parameters. It can consume
 * data within the user's context, such as AWS credentials or local SSH keys, that will be passed to the server
 * side implementation.
 */
interface RemoteClient {

    /**
     * Returns the canonical name of this provider, such as "ssh" or "s3". This must be globally unique, and must
     * match the leading URI component (ssh://...).
     */
    fun getProvider(): String

    /**
     * Parse a URI and return the provider-specific remote parameters in structured form. These properties will be
     * preserved as part of the remote metadata on the server and passed to subsequent server-side operations. The
     * additional properties map can contain properties specified by the user that don't fit the URI format well,
     * such as "-p keyFile=/path/to/sshKey". This should throw an IllegalArgumentException for a bad URI format
     * or invalid properties.
     */
    fun parseUri(uri: URI, additionalProperties: Map<String, String>): Map<String, String>

    /**
     * Convert a remote back into URI form for display. Since this is for display only, any sensitive information
     * should be redacted (e.g. "user:****@host"). Any properties that cannot be represented in the URI can be
     * passed back as the second part of the pair.
     */
    fun toUri(properties: Map<String, String>): Pair<String, Map<String, String>>
}
