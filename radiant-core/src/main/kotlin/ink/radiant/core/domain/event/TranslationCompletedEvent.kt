package ink.radiant.core.domain.event

import ink.radiant.core.domain.model.SentencePair
import ink.radiant.core.domain.model.TranslationMetadata
import ink.radiant.core.domain.model.TranslationSessionId
import java.time.Instant
import java.util.UUID

data class TranslationCompletedEvent(
    val sessionId: TranslationSessionId,
    val translatedText: String,
    val sentencePairs: List<SentencePair>,
    val resultMetadata: TranslationMetadata,
    val completedAt: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID(),
    override val aggregateId: String = sessionId.value,
    override val eventType: String = EVENT_TYPE,
    override val occurredAt: Instant = completedAt,
    override val version: Int = 1,
    override val metadata: EventMetadata = EventMetadata(),
) : DomainEvent {

    init {
        require(translatedText.isNotBlank()) { "Translated text must not be blank" }
        require(sentencePairs.isNotEmpty()) { "Sentence pairs must not be empty" }
    }

    companion object {
        const val EVENT_TYPE = "TranslationCompleted"
    }
}
