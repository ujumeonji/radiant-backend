package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

data class PostUpdatedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String,
    override val eventType: String = "PostUpdated",
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
    val postId: String,
    val field: String,
    val oldValue: Any?,
    val newValue: Any?,
    val updatedBy: String,
) : DomainEvent
