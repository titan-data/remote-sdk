package io.titandata.remote

/**
 * Event types for progress updates. This is a subset of all types that can be posted in the server, because the
 * terminal states (error, abort, complete) are handled by the titan server infrastructure.
 */
enum class RemoteProgress {
    MESSAGE, // Singular message, just display and move on.
    START, // Start of a longer running operation, where multiple progress message may appear followed by END
    PROGRESS, // Progress update within a (START, END) pair
    END // End of a START operation
}

/**
 * Operation type.
 */
enum class RemoteOperationType {
    PUSH,
    PULL
}

/**
 * All data associated with an operation:
 *
 *      updateProgress      Callback to post progress updates back to the server Comprises a type, optional message,
 *                          and optional percentage (0-100)
 *
 *                          Type        Always required.
 *                          Message     Required for MESSAGE and START. If not specified for PROGRESS, then percent
 *                                      must be included
 *                          Percent     Optional for PROGRESS
 *
 *      remote              Remote configuration
 *
 *      parameters          Remote parameters
 *
 *      operationId         Unique identifier for this operation
 *
 *      commitId            Commit being pushed or pulled
 *
 *      type                Operation type (push or pull)
 *
 *      userData            Optional data, set by startOperation(), that can be used during the course of the operation.
 */
data class RemoteOperation(
    val updateProgress: (RemoteProgress, String?, Int?) -> Unit,
    val remote: Map<String, Any>,
    val parameters: Map<String, Any>,
    val operationId: String,
    val commidId: String,
    val type: RemoteOperationType,
    var data: Any?
)

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

    /**
     * Starts a new operation. This method is invoked once for each operation, and is passed all context around
     * the operation, including remote configuration, commit ID, and unique identifier for the operation. This
     * method may do nothing, but if needed it can do additional work and store operation-specific data in the
     * 'data' member of the object.
     */
    fun startOperation(operation: RemoteOperation)

    /**
     * Ends an operation. This is called after all volumes have been synced, regardless of success or failure. The
     * 'isSuccessful' flag indicates whether the operation was successful.
     */
    fun endOperation(operation: RemoteOperation, isSuccessful: Boolean)

    /**
     * Syncs a particular volume, push or pull. The operation type can be determined by the 'type' member of the
     * operation data. The volume data will be accessible at the 'volumePath' location. The 'scratchPath' is created
     * once per operation, and can be used to store any temporary state, even across operations.
     */
    fun syncVolume(operation: RemoteOperation, volumeName: String, volumeDescription: String, volumePath: String, scratchPath: String)
}
