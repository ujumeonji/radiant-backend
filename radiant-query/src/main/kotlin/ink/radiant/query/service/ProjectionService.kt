package ink.radiant.query.service

import ink.radiant.core.domain.event.DomainEvent
import ink.radiant.core.domain.repository.EventStoreRepository
import ink.radiant.query.handler.EventHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProjectionService(
    private val eventStoreRepository: EventStoreRepository,
    private val eventHandlers: List<EventHandler<*>>,
) {
    private val handlerMap: Map<Class<*>, EventHandler<*>> =
        eventHandlers.associateBy { it.getEventType() }

    fun rebuildAllProjections() {
        val allEvents = eventStoreRepository.getAllEvents()

        allEvents.forEach { event ->
            processEvent(event)
        }
    }

    fun processEventsFrom(fromVersion: Long) {
        val events = eventStoreRepository.getAllEventsFrom(fromVersion)

        events.forEach { event ->
            processEvent(event)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun processEvent(event: DomainEvent) {
        val handler = handlerMap[event::class.java] as? EventHandler<DomainEvent>
        handler?.handle(event)
    }
}
