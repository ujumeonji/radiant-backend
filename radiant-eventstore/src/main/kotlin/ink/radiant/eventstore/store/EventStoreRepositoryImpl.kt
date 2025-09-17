package ink.radiant.eventstore.store

import ink.radiant.core.common.exception.ConcurrencyException
import ink.radiant.core.common.exception.EventStoreException
import ink.radiant.core.domain.event.DomainEvent
import ink.radiant.core.domain.repository.EventStoreRepository
import ink.radiant.eventstore.entity.EventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface EventEntityRepository : JpaRepository<EventEntity, UUID> {
    fun findByAggregateIdOrderByVersionAsc(aggregateId: String): List<EventEntity>

    fun findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(aggregateId: String, version: Long): List<EventEntity>

    @Query("SELECT MAX(e.version) FROM EventEntity e WHERE e.aggregateId = :aggregateId")
    fun findMaxVersionByAggregateId(aggregateId: String): Long?

    @Query("SELECT e FROM EventEntity e ORDER BY e.version ASC")
    fun findAllOrderByVersionAsc(): List<EventEntity>

    @Query("SELECT e FROM EventEntity e WHERE e.version > :fromVersion ORDER BY e.version ASC")
    fun findAllFromVersionOrderByVersionAsc(fromVersion: Long): List<EventEntity>
}

@Service
@Transactional
class EventStoreRepositoryImpl(
    private val eventEntityRepository: EventEntityRepository,
    private val eventSerializer: EventSerializer,
) : EventStoreRepository {

    override fun saveEvents(aggregateId: String, events: List<DomainEvent>, expectedVersion: Long) {
        if (events.isEmpty()) return

        val currentVersion = eventEntityRepository.findMaxVersionByAggregateId(aggregateId) ?: 0
        if (currentVersion != expectedVersion) {
            throw ConcurrencyException(aggregateId, expectedVersion, currentVersion)
        }

        try {
            val eventEntities = events.mapIndexed { index, event ->
                EventEntity(
                    eventId = event.eventId,
                    aggregateId = aggregateId,
                    eventType = event.eventType,
                    version = expectedVersion + index + 1,
                    eventData = eventSerializer.serialize(event),
                    metadata = eventSerializer.serializeMetadata(event.metadata),
                    occurredAt = event.occurredAt,
                    eventVersion = event.version,
                )
            }

            eventEntityRepository.saveAll(eventEntities)
        } catch (e: Exception) {
            throw EventStoreException("Failed to save events for aggregate: $aggregateId", e)
        }
    }

    @Transactional(readOnly = true)
    override fun getEvents(aggregateId: String): List<DomainEvent> {
        return eventEntityRepository
            .findByAggregateIdOrderByVersionAsc(aggregateId)
            .map { entity ->
                eventSerializer.deserialize<DomainEvent>(entity.eventData, entity.eventType)
            }
    }

    @Transactional(readOnly = true)
    override fun getEvents(aggregateId: String, fromVersion: Long): List<DomainEvent> {
        return eventEntityRepository
            .findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(aggregateId, fromVersion)
            .map { entity ->
                eventSerializer.deserialize<DomainEvent>(entity.eventData, entity.eventType)
            }
    }

    @Transactional(readOnly = true)
    override fun getAllEvents(): List<DomainEvent> {
        return eventEntityRepository
            .findAllOrderByVersionAsc()
            .map { entity ->
                eventSerializer.deserialize<DomainEvent>(entity.eventData, entity.eventType)
            }
    }

    @Transactional(readOnly = true)
    override fun getAllEventsFrom(fromVersion: Long): List<DomainEvent> {
        return eventEntityRepository
            .findAllFromVersionOrderByVersionAsc(fromVersion)
            .map { entity ->
                eventSerializer.deserialize<DomainEvent>(entity.eventData, entity.eventType)
            }
    }
}
