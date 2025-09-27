package ink.radiant.core.domain.model

import java.time.Instant

data class TranslationSession(
    val id: TranslationSessionId,
    val userId: String,
    val sourceLanguage: Language,
    val sourceText: String,
    val textLength: Int,
    val status: TranslationStatus,
    val createdAt: Instant,
    val completedAt: Instant?,
    val failure: Failure?,
) {
    data class Failure(
        val type: TranslationErrorType,
        val message: String,
    )
}
