package ink.radiant.core.domain.event

import ink.radiant.core.domain.model.Language
import ink.radiant.core.domain.model.TranslationSessionId
import java.time.Instant
import java.util.UUID

data class TranslationRequestedEvent(
    val sessionId: TranslationSessionId,
    val userId: String,
    val sourceLanguage: Language,
    val sourceText: String,
    val textLength: Int,
    val requestedAt: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String = sessionId.value,
    override val eventType: String = EVENT_TYPE,
    override val occurredAt: Instant = requestedAt,
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
) : DomainEvent {

    init {
        require(sourceText.isNotBlank()) { "Source text must not be blank" }
        require(textLength == sourceText.length) { "Text length must match source text length" }
        require(textLength > 0) { "Text length must be greater than zero" }
    }

    companion object {
        const val EVENT_TYPE = "TranslationRequested"
    }
}
