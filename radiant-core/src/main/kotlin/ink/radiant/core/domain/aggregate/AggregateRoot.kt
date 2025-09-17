package ink.radiant.core.domain.aggregate

import ink.radiant.core.domain.event.DomainEvent

abstract class AggregateRoot<T : AggregateId>(
    val id: T,
) {
    private val _uncommittedEvents = mutableListOf<DomainEvent>()

    var version: Long = 0

    val uncommittedEvents: List<DomainEvent>
        get() = _uncommittedEvents.toList()

    protected fun applyEvent(event: DomainEvent) {
        applyEventInternal(event)
        _uncommittedEvents.add(event)
    }

    fun replayEvent(event: DomainEvent) {
        applyEventInternal(event)
        version++
    }

    fun markEventsAsCommitted() {
        _uncommittedEvents.clear()
    }

    protected abstract fun applyEventInternal(event: DomainEvent)
}
