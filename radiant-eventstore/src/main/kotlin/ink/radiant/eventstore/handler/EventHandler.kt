package ink.radiant.eventstore.handler

import ink.radiant.core.domain.event.DomainEvent

interface EventHandler<T : DomainEvent> {
    fun handle(event: T)

    fun getEventType(): Class<T>
}
