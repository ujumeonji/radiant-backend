package ink.radiant.command.command

import ink.radiant.core.domain.exception.TranslationInputException
import ink.radiant.core.domain.exception.TranslationLengthExceededException
import ink.radiant.core.domain.model.LanguageCode

data class RequestTranslationCommand(
    val userId: String,
    val sourceText: String,
    val sourceLanguageHint: LanguageCode? = null,
    val preserveFormatting: Boolean = true,
    val targetLanguage: LanguageCode = LanguageCode.KO,
) {
    init {
        if (userId.isBlank()) {
            throw TranslationInputException("User id must not be blank")
        }
        if (sourceText.isBlank()) {
            throw TranslationInputException("Source text must not be blank")
        }
        if (sourceText.length > MAX_TEXT_LENGTH) {
            throw TranslationLengthExceededException(sourceText.length, MAX_TEXT_LENGTH)
        }
    }

    companion object {
        const val MAX_TEXT_LENGTH = 20_000
    }
}
