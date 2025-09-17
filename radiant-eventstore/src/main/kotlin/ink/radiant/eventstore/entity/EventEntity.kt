package ink.radiant.eventstore.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "event_store",
    indexes = [
        Index(name = "idx_aggregate_id", columnList = "aggregateId"),
        Index(name = "idx_aggregate_version", columnList = "aggregateId, version"),
        Index(name = "idx_event_type", columnList = "eventType"),
        Index(name = "idx_occurred_at", columnList = "occurredAt"),
    ],
)
class EventEntity(
    @Id
    @Column(columnDefinition = "UUID")
    val eventId: UUID,

    @Column(nullable = false)
    val aggregateId: String,

    @Column(nullable = false)
    val eventType: String,

    @Column(nullable = false)
    val version: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val eventData: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val metadata: String,

    @Column(nullable = false)
    val occurredAt: Instant,

    @Column(nullable = false)
    val eventVersion: Int = 1,
) {
    constructor() : this(
        eventId = UUID.randomUUID(),
        aggregateId = "",
        eventType = "",
        version = 0,
        eventData = "",
        metadata = "",
        occurredAt = Instant.now(),
        eventVersion = 1,
    )
}
