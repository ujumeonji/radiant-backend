package ink.radiant.core.domain.event

import ink.radiant.core.domain.model.TranslationErrorType
import ink.radiant.core.domain.model.TranslationSessionId
import java.time.Instant
import java.util.UUID

data class TranslationFailedEvent(
    val sessionId: TranslationSessionId,
    val errorType: TranslationErrorType,
    val errorMessage: String,
    val failedAt: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String = sessionId.value,
    override val eventType: String = EVENT_TYPE,
    override val occurredAt: Instant = failedAt,
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
) : DomainEvent {

    init {
        require(errorMessage.isNotBlank()) { "Error message cannot be blank" }
    }

    companion object {
        const val EVENT_TYPE = "TranslationFailed"
    }
}
