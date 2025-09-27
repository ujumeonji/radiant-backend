package ink.radiant.infrastructure.ai

import ink.radiant.core.domain.model.TranslationErrorType

class TranslationProviderException(
    val errorType: TranslationErrorType,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
