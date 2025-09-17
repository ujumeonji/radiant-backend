package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val eventId: UUID
    val aggregateId: String
    val eventType: String
    val occurredAt: Instant
    val version: Int
    val metadata: EventMetadata
}
