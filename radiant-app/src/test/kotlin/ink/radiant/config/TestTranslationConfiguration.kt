package ink.radiant.config

import ink.radiant.core.domain.model.LanguageCode
import ink.radiant.core.domain.model.SentencePair
import ink.radiant.core.domain.model.TranslationMetadata
import ink.radiant.infrastructure.ai.TranslationModelClient
import ink.radiant.infrastructure.ai.TranslationModelRequest
import ink.radiant.infrastructure.ai.TranslationModelResponse
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestTranslationConfiguration {

    @Bean
    @Primary
    fun stubTranslationModelClient(): TranslationModelClient {
        return object : TranslationModelClient {
            override fun translate(request: TranslationModelRequest): TranslationModelResponse {
                val translatedText = resolveTranslation(request.sourceText, request.sourceLanguage)
                val sentencePairs = listOf(
                    SentencePair(
                        order = 0,
                        original = request.sourceText,
                        translated = translatedText,
                    ),
                )

                val metadata = TranslationMetadata(
                    processingTimeMs = 25,
                    tokenCount = 64,
                    chunkCount = 1,
                )

                return TranslationModelResponse(
                    translatedText = translatedText,
                    sentencePairs = sentencePairs,
                    metadata = metadata,
                )
            }
        }
    }

    private fun resolveTranslation(text: String, language: LanguageCode): String {
        return when (language) {
            LanguageCode.EN -> "안녕하세요 세계! 이것은 테스트 번역입니다."
            LanguageCode.JA -> "안녕하세요, 이것은 일본어 문장의 테스트 번역입니다."
            LanguageCode.ZH -> "안녕하세요, 이것은 중국어 문장의 테스트 번역입니다."
            LanguageCode.KO -> text
            else -> "번역된 문장"
        }
    }
}
