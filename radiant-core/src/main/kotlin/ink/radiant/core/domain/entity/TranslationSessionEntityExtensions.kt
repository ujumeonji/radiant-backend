package ink.radiant.core.domain.entity

import ink.radiant.core.domain.model.Language
import ink.radiant.core.domain.model.LanguageCode
import ink.radiant.core.domain.model.TranslationMetadata
import ink.radiant.core.domain.model.TranslationResult
import ink.radiant.core.domain.model.TranslationSession
import ink.radiant.core.domain.model.TranslationSessionId
import ink.radiant.core.domain.model.TranslationStatus

fun TranslationSessionEntity.toDomainSession(): TranslationSession {
    return TranslationSession(
        id = TranslationSessionId.from(id),
        userId = userId,
        sourceLanguage = toLanguage(),
        sourceText = sourceText,
        textLength = textLength,
        status = status,
        createdAt = requestedAt.toInstant(),
        completedAt = completedAt?.toInstant(),
        failure = failureType?.let { type ->
            TranslationSession.Failure(
                type = type,
                message = failureMessage ?: "",
            )
        },
    )
}

fun TranslationSessionEntity.toResult(): TranslationResult? {
    if (status != TranslationStatus.COMPLETED) {
        return null
    }

    val translated = translatedText ?: return null
    val processingTime = metadataProcessingTimeMs ?: return null
    val tokenCount = metadataTokenCount ?: return null
    val chunkCount = metadataChunkCount ?: return null

    return TranslationResult(
        sessionId = TranslationSessionId.from(id),
        sourceLanguage = toLanguage(),
        sentencePairs = sentencePairs.map { pair -> pair.toDomain() },
        translatedText = translated,
        metadata = TranslationMetadata(
            processingTimeMs = processingTime,
            tokenCount = tokenCount,
            chunkCount = chunkCount,
        ),
    )
}

private fun TranslationSessionEntity.toLanguage(): Language {
    val code = runCatching { LanguageCode.valueOf(sourceLanguageCode) }
        .getOrDefault(LanguageCode.UNKNOWN)
    val name = sourceLanguageName.ifBlank { code.displayName }

    return Language(
        code = code,
        name = name,
        confidence = sourceLanguageConfidence,
    )
}
