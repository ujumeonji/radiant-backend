package ink.radiant.core.domain.entity

import ink.radiant.core.domain.model.Language
import ink.radiant.core.domain.model.SentencePair
import ink.radiant.core.domain.model.TranslationErrorType
import ink.radiant.core.domain.model.TranslationMetadata
import ink.radiant.core.domain.model.TranslationSessionId
import ink.radiant.core.domain.model.TranslationStatus
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Entity
@Table(name = "translation_sessions")
class TranslationSessionEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: String,

    @Column(name = "source_language_code", nullable = false, updatable = false)
    var sourceLanguageCode: String,

    @Column(name = "source_language_name", nullable = false, updatable = false)
    var sourceLanguageName: String,

    @Column(name = "source_language_confidence")
    var sourceLanguageConfidence: Double? = null,

    @Column(name = "source_text", columnDefinition = "TEXT", nullable = false, updatable = false)
    var sourceText: String,

    @Column(name = "text_length", nullable = false)
    var textLength: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TranslationStatus,

    @Column(name = "requested_at", nullable = false, updatable = false)
    var requestedAt: OffsetDateTime,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    @Column(name = "translated_text", columnDefinition = "TEXT")
    var translatedText: String? = null,

    @Column(name = "metadata_processing_time_ms")
    var metadataProcessingTimeMs: Long? = null,

    @Column(name = "metadata_token_count")
    var metadataTokenCount: Int? = null,

    @Column(name = "metadata_chunk_count")
    var metadataChunkCount: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_type")
    var failureType: TranslationErrorType? = null,

    @Column(name = "failure_message", columnDefinition = "TEXT")
    var failureMessage: String? = null,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "translation_sentence_pairs",
        joinColumns = [JoinColumn(name = "session_id")],
    )
    @OrderColumn(name = "pair_index")
    var sentencePairs: MutableList<TranslationSentencePairEmbeddable> = mutableListOf(),
) : BaseEntity() {

    fun markCompleted(
        translatedText: String,
        sentencePairs: List<SentencePair>,
        metadata: TranslationMetadata,
        completedAt: Instant,
    ) {
        check(status == TranslationStatus.IN_PROGRESS) { "Cannot complete session in status $status" }
        require(translatedText.isNotBlank()) { "Translated text must not be blank" }
        require(sentencePairs.isNotEmpty()) { "Sentence pairs must not be empty" }

        this.status = TranslationStatus.COMPLETED
        this.completedAt = completedAt.toOffsetDateTime()
        this.translatedText = translatedText
        this.metadataProcessingTimeMs = metadata.processingTimeMs
        this.metadataTokenCount = metadata.tokenCount
        this.metadataChunkCount = metadata.chunkCount
        this.failureType = null
        this.failureMessage = null
        val orderedPairs = sentencePairs
            .sortedBy { pair -> pair.order }
            .map { pair -> TranslationSentencePairEmbeddable.from(pair) }
        this.sentencePairs.apply {
            clear()
            addAll(orderedPairs)
        }
    }

    fun markFailed(errorType: TranslationErrorType, errorMessage: String, failedAt: Instant) {
        check(status == TranslationStatus.IN_PROGRESS) { "Cannot fail session in status $status" }
        require(errorMessage.isNotBlank()) { "Error message must not be blank" }

        this.status = TranslationStatus.FAILED
        this.completedAt = failedAt.toOffsetDateTime()
        this.failureType = errorType
        this.failureMessage = errorMessage
        this.translatedText = null
        this.metadataProcessingTimeMs = null
        this.metadataTokenCount = null
        this.metadataChunkCount = null
        this.sentencePairs.clear()
    }

    companion object {
        private const val MAX_TEXT_LENGTH = 20_000

        fun create(
            sessionId: TranslationSessionId,
            userId: String,
            language: Language,
            sourceText: String,
            requestedAt: Instant,
        ): TranslationSessionEntity {
            require(userId.isNotBlank()) { "User id must not be blank" }
            require(sourceText.isNotBlank()) { "Source text must not be blank" }
            require(sourceText.length <= MAX_TEXT_LENGTH) {
                "Source text exceeds allowed length of $MAX_TEXT_LENGTH"
            }

            return TranslationSessionEntity(
                id = sessionId.value,
                userId = userId,
                sourceLanguageCode = language.code.name,
                sourceLanguageName = language.name,
                sourceLanguageConfidence = language.confidence,
                sourceText = sourceText,
                textLength = sourceText.length,
                status = TranslationStatus.IN_PROGRESS,
                requestedAt = requestedAt.toOffsetDateTime(),
            )
        }
    }
}

@Embeddable
data class TranslationSentencePairEmbeddable(
    @Column(name = "pair_order", nullable = false)
    var orderValue: Int = 0,

    @Column(name = "original_text", columnDefinition = "TEXT", nullable = false)
    var original: String = "",

    @Column(name = "translated_text", columnDefinition = "TEXT", nullable = false)
    var translated: String = "",
) {
    fun toDomain(): SentencePair {
        return SentencePair(
            order = orderValue,
            original = original,
            translated = translated,
        )
    }

    companion object {
        fun from(pair: SentencePair): TranslationSentencePairEmbeddable {
            return TranslationSentencePairEmbeddable(
                orderValue = pair.order,
                original = pair.original,
                translated = pair.translated,
            )
        }
    }
}

private fun Instant.toOffsetDateTime(): OffsetDateTime {
    return OffsetDateTime.ofInstant(this, ZoneOffset.UTC)
}
