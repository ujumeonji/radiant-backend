package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

data class PostViewedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String,
    override val eventType: String = "PostViewed",
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
    val postId: String,
    val viewedAt: Instant = Instant.now(),
) : DomainEvent
