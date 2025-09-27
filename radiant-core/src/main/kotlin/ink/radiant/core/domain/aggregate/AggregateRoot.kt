package ink.radiant.core.domain.aggregate

import ink.radiant.core.domain.entity.BaseEntity
import ink.radiant.core.domain.event.DomainEvent
import jakarta.persistence.Transient

abstract class AggregateRoot : BaseEntity() {
    @Transient
    protected val internalUncommittedEvents = mutableListOf<DomainEvent>()

    @Transient
    var aggregateVersion: Long = 0

    val uncommittedEvents: List<DomainEvent> get() =
        internalUncommittedEvents.toList()

    fun markEventsAsCommitted() {
        internalUncommittedEvents.clear()
    }

    protected fun applyEvent(event: DomainEvent) {
        internalUncommittedEvents.add(event)
    }
}
