package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

data class AccountDeactivatedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String,
    override val eventType: String = "AccountDeactivated",
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
    val accountId: String,
    val deactivatedBy: String,
    val reason: String? = null,
) : DomainEvent
