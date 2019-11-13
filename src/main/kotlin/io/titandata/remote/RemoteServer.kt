package io.titandata.remote

/**
 * The remote client interface defines functionality that runs in the context of the titan server. It is responsible for
 * fetching remote commits and running push / pull operations.
 */
interface RemoteServer {

    /**
     * Returns the canonical name of this provider, such as "ssh" or "s3". This must be globally unique, and must
     * match the name used in the corresponding client.
     */
    fun getProvider(): String

    /**
     * Fetches a set of commits from the remote server. Commits are simply a tuple of (commitId, properties), with
     * some properties having semantic significance (namely timestamp and tags). The remote provider should always
     * return commits in reverse timestamp order, optionally filtered by the given tags. There are utility methods
     * in RemoteServerUtil if remotes don't provide this functionality server-side. Tags are specified as a list of
     * pairs, where the first element is always the key and the second is optionally the value.
     *
     * There is not yet support for pagination, though that will be added in the future to avoid having to fetch
     * the entire commit history every time.
     */
    fun listCommits(remote: Map<String, Any>, parameters: Map<String, Any>, tags: List<Pair<String, String?>>): List<Pair<String, Map<String, Any>>>

    /**
     * Fetches a single commit from the given remote. Returns null if no such commit exists.
     */
    fun getCommit(remote: Map<String, Any>, parameters: Map<String, Any>, commitId: String): Map<String, Any>?
}
