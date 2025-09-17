package ink.radiant.infrastructure.messaging

import ink.radiant.core.domain.event.DomainEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
abstract class EventSubscriber {

    @Async
    @EventListener
    open fun handleDomainEvent(event: DomainEvent) {
        onDomainEvent(event)
    }

    protected abstract fun onDomainEvent(event: DomainEvent)
}
