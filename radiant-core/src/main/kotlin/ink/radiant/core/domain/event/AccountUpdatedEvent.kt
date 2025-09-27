package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

data class AccountUpdatedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String,
    override val eventType: String = "AccountUpdated",
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
    val accountId: String,
    val field: String,
    val oldValue: Any?,
    val newValue: Any?,
    val updatedBy: String,
) : DomainEvent
