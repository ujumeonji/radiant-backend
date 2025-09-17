package ink.radiant.core.domain.repository

import ink.radiant.core.domain.event.DomainEvent

interface EventStoreRepository {
    fun saveEvents(aggregateId: String, events: List<DomainEvent>, expectedVersion: Long)

    fun getEvents(aggregateId: String): List<DomainEvent>

    fun getEvents(aggregateId: String, fromVersion: Long): List<DomainEvent>

    fun getAllEvents(): List<DomainEvent>

    fun getAllEventsFrom(fromVersion: Long): List<DomainEvent>
}
