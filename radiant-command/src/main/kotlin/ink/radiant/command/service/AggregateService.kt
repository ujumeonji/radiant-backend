package ink.radiant.command.service

import ink.radiant.core.common.exception.AggregateNotFoundException
import ink.radiant.core.domain.aggregate.AggregateId
import ink.radiant.core.domain.aggregate.AggregateRoot
import ink.radiant.core.domain.repository.EventStoreRepository
import org.springframework.stereotype.Service

@Service
class AggregateService(
    private val eventStoreRepository: EventStoreRepository,
) {
    fun <T : AggregateRoot<out AggregateId>> loadAggregate(aggregateId: String, aggregateFactory: (String) -> T): T {
        val events = eventStoreRepository.getEvents(aggregateId)

        if (events.isEmpty()) {
            throw AggregateNotFoundException(aggregateId)
        }

        val aggregate = aggregateFactory(aggregateId)
        events.forEach { event ->
            aggregate.replayEvent(event)
        }

        return aggregate
    }

    fun <T : AggregateRoot<out AggregateId>> saveAggregate(aggregate: T) {
        val uncommittedEvents = aggregate.uncommittedEvents

        if (uncommittedEvents.isNotEmpty()) {
            eventStoreRepository.saveEvents(
                aggregateId = aggregate.id.value,
                events = uncommittedEvents,
                expectedVersion = aggregate.version,
            )

            aggregate.markEventsAsCommitted()
        }
    }

    fun isNewAggregate(aggregateId: String): Boolean {
        return eventStoreRepository.getEvents(aggregateId).isEmpty()
    }
}
