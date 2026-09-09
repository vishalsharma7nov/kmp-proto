package io.github.vishalsharma7nov.kmpproto.offline

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public data class OfflineQueueOptions(
    val maxSize: Int = 100,
)

public data class QueuedMutation(
    val id: String,
    val rpcPath: String,
    val request: Map<String, Any?>,
    val enqueuedAtMs: Long,
)

internal expect fun currentTimeMillis(): Long

/**
 * In-memory offline mutation queue. Persist externally if you need durability across process restarts.
 */
public class OfflineMutationQueue(
    private val options: OfflineQueueOptions = OfflineQueueOptions(),
    private val clockMs: () -> Long = { currentTimeMillis() },
) {
    private val mutex = Mutex()
    private val items = ArrayDeque<QueuedMutation>()

    public suspend fun enqueue(rpcPath: String, request: Map<String, Any?>): QueuedMutation =
        mutex.withLock {
            if (items.size >= options.maxSize) {
                items.removeFirst()
            }
            val item = QueuedMutation(
                id = "${clockMs()}-${items.size}",
                rpcPath = rpcPath,
                request = request,
                enqueuedAtMs = clockMs(),
            )
            items.addLast(item)
            item
        }

    public suspend fun peekAll(): List<QueuedMutation> = mutex.withLock { items.toList() }

    public suspend fun dequeue(): QueuedMutation? = mutex.withLock {
        if (items.isEmpty()) null else items.removeFirst()
    }

    public suspend fun clear(): Unit = mutex.withLock { items.clear() }

    public suspend fun size(): Int = mutex.withLock { items.size }
}
