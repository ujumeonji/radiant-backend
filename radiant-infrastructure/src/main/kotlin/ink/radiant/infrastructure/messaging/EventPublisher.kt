package ink.radiant.infrastructure.messaging

import ink.radiant.core.domain.event.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun publish(event: DomainEvent) {
        applicationEventPublisher.publishEvent(event)
    }

    fun publishAll(events: List<DomainEvent>) {
        events.forEach { event ->
            publish(event)
        }
    }
}
