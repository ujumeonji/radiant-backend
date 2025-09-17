package ink.radiant.core.common.exception

abstract class DomainException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class AggregateNotFoundException(aggregateId: String) : DomainException("Aggregate not found: $aggregateId")

class ConcurrencyException(
    aggregateId: String,
    expectedVersion: Long,
    actualVersion: Long,
) : DomainException(
    "Concurrency conflict for aggregate $aggregateId. " +
        "Expected version: $expectedVersion, Actual version: $actualVersion",
)

class EventStoreException(message: String, cause: Throwable? = null) : DomainException(message, cause)
