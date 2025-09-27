package ink.radiant.core.domain.model

data class TranslationResult(
    val sessionId: TranslationSessionId,
    val sourceLanguage: Language,
    val sentencePairs: List<SentencePair>,
    val translatedText: String,
    val metadata: TranslationMetadata,
)
