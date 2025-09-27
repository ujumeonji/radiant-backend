package ink.radiant.core.domain.model

enum class LanguageCode(val displayName: String) {
    EN("English"),
    JA("Japanese"),
    ZH("Chinese"),
    KO("Korean"),
    UNKNOWN("Unknown"),
    ;

    companion object {
        fun from(value: String): LanguageCode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported language code: $value")
        }
    }
}

data class Language(
    val code: LanguageCode,
    val name: String = code.displayName,
    val confidence: Double? = null,
) {
    init {
        if (confidence != null) {
            require(confidence in 0.0..1.0) { "Confidence must be between 0.0 and 1.0" }
        }
    }

    companion object {
        fun fromCode(code: LanguageCode, confidence: Double? = null): Language {
            return Language(code = code, confidence = confidence)
        }

        fun fromCode(code: String, confidence: Double? = null): Language {
            val languageCode = runCatching { LanguageCode.from(code) }.getOrDefault(LanguageCode.UNKNOWN)
            val name = if (languageCode == LanguageCode.UNKNOWN) code.uppercase() else languageCode.displayName
            return Language(code = languageCode, name = name, confidence = confidence)
        }
    }
}
