package ink.radiant.infrastructure.ai

import ink.radiant.core.domain.model.Language
import ink.radiant.core.domain.model.LanguageCode
import org.springframework.stereotype.Service

@Service
class LanguageDetectionService {

    fun detectLanguage(text: String, hint: LanguageCode?): Language {
        hint?.let { return Language.fromCode(it, confidence = 1.0) }

        val normalized = text.trim()
        if (normalized.isEmpty()) {
            return Language.fromCode(LanguageCode.EN, confidence = 0.0)
        }

        return when {
            containsJapaneseCharacters(normalized) -> Language(
                code = LanguageCode.JA,
                confidence = 0.95,
            )
            containsChineseCharacters(normalized) -> Language(
                code = LanguageCode.ZH,
                confidence = 0.95,
            )
            containsKoreanCharacters(normalized) -> Language(
                code = LanguageCode.KO,
                confidence = 0.9,
            )
            else -> Language(
                code = LanguageCode.EN,
                confidence = 0.85,
            )
        }
    }

    private fun containsJapaneseCharacters(text: String): Boolean {
        return text.any { char ->
            char.code in HIRAGANA_RANGE || char.code in KATAKANA_RANGE
        }
    }

    private fun containsChineseCharacters(text: String): Boolean {
        return text.any { char ->
            val block = Character.UnicodeBlock.of(char)
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
        }
    }

    private fun containsKoreanCharacters(text: String): Boolean {
        return text.any { char ->
            char.code in HANGUL_RANGE
        }
    }

    companion object {
        private val HIRAGANA_RANGE = 0x3040..0x309F
        private val KATAKANA_RANGE = 0x30A0..0x30FF
        private val HANGUL_RANGE = 0xAC00..0xD7A3
    }
}
