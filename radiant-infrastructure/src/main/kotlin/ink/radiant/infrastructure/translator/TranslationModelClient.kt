package ink.radiant.infrastructure.translator

import ink.radiant.core.domain.model.LanguageCode
import ink.radiant.core.domain.model.SentencePair
import ink.radiant.core.domain.model.TranslationMetadata

interface TranslationModelClient {
    fun translate(request: TranslationModelRequest): TranslationModelResponse
}

data class TranslationModelRequest(
    val sessionId: String,
    val sourceLanguage: LanguageCode,
    val targetLanguage: LanguageCode,
    val sourceText: String,
    val preserveFormatting: Boolean,
)

data class TranslationModelResponse(
    val translatedText: String,
    val sentencePairs: List<SentencePair>,
    val metadata: TranslationMetadata,
)
