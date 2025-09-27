package ink.radiant.core.domain.event

import java.time.Instant
import java.util.UUID

data class AccountLoginEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String,
    override val eventType: String = "AccountLogin",
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
    val accountId: String,
    val loginTime: Instant = Instant.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
) : DomainEvent
