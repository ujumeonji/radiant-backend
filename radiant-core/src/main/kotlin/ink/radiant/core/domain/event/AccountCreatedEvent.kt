package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

data class AccountCreatedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String,
    override val eventType: String = "AccountCreated",
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
    val accountId: String,
    val email: String,
    val name: String,
    val provider: String,
    val providerId: String,
    val displayName: String,
    val avatarUrl: String? = null,
) : DomainEvent
