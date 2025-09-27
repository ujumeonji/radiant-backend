package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

data class PostDeletedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String,
    override val eventType: String = "PostDeleted",
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
    val postId: String,
    val deletedBy: String?,
) : DomainEvent
