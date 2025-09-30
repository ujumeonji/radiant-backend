package ink.radiant.infrastructure.translator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ink.radiant.core.domain.model.LanguageCode
import ink.radiant.core.domain.model.SentencePair
import ink.radiant.core.domain.model.TranslationErrorType
import ink.radiant.core.domain.model.TranslationMetadata
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.stereotype.Service

@Service
class TranslationService(
    private val chatClient: ChatModel,
    private val objectMapper: ObjectMapper,
) : TranslationModelClient {

    override fun translate(request: TranslationModelRequest): TranslationModelResponse {
        val startTime = System.currentTimeMillis()
        val prompt = buildPrompt(request)

        val rawResponse = try {
            chatClient.call(prompt).result.output.text
        } catch (exception: Exception) {
            throw TranslationProviderException(
                errorType = TranslationErrorType.SERVICE_UNAVAILABLE,
                message = "Failed to call translation model",
                cause = exception,
            )
        }

        val sanitized = sanitizeResponse(rawResponse)

        val structured = try {
            objectMapper.readValue<StructuredTranslationResponse>(sanitized)
        } catch (exception: Exception) {
            throw TranslationProviderException(
                errorType = TranslationErrorType.UNKNOWN_ERROR,
                message = "Failed to parse translation response",
                cause = exception,
            )
        }

        if (structured.sentencePairs.isNullOrEmpty()) {
            throw TranslationProviderException(
                errorType = TranslationErrorType.UNKNOWN_ERROR,
                message = "Translation response did not contain sentence pairs",
            )
        }

        val orderedPairs = structured.sentencePairs.mapIndexed { index, pair ->
            SentencePair(
                order = index,
                original = pair.original,
                translated = pair.translated,
            )
        }

        val metadata = buildMetadata(structured.metadata, orderedPairs.size, startTime)

        return TranslationModelResponse(
            translatedText = structured.translatedText,
            sentencePairs = orderedPairs,
            metadata = metadata,
        )
    }

    private fun buildPrompt(request: TranslationModelRequest): Prompt {
        val template = PromptTemplate(
            """
            Translate the provided content from {sourceLanguage} to {targetLanguage}.
            Preserve the original formatting, including lists, headings, and whitespace.
            Maintain the sentence order and meaning. If the source includes code blocks or URLs, keep them intact.
            Respond in JSON format with fields translatedText, sentencePairs (array of objects with original and translated), and metadata (processingTimeMs, tokenCount, chunkCount).
            Source Text:
            {sourceText}
            Preserve Formatting: {preserveFormatting}
            """.trimIndent(),
        )

        return template.create(
            mapOf(
                "sourceLanguage" to describeLanguage(request.sourceLanguage),
                "targetLanguage" to describeLanguage(request.targetLanguage),
                "sourceText" to request.sourceText,
                "preserveFormatting" to request.preserveFormatting,
            ),
        )
    }

    private fun sanitizeResponse(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("```")) {
            val lines = trimmed.lines()
            val content = lines.dropWhile { !it.trim().startsWith("{") }
                .dropLastWhile { !it.trim().endsWith("}") }
            return content.joinToString("\n")
        }

        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        if (startIndex in 0..<endIndex) {
            return trimmed.substring(startIndex, endIndex + 1)
        }

        return trimmed
    }

    private fun buildMetadata(
        metadata: StructuredTranslationMetadata?,
        sentenceCount: Int,
        startTime: Long,
    ): TranslationMetadata {
        val processingTimeMs = metadata?.processingTimeMs ?: (System.currentTimeMillis() - startTime)
        val tokenCount = metadata?.tokenCount ?: (sentenceCount * AVERAGE_TOKENS_PER_SENTENCE)
        val chunkCount = metadata?.chunkCount ?: sentenceCount.coerceAtLeast(1)

        return TranslationMetadata(
            processingTimeMs = processingTimeMs,
            tokenCount = tokenCount,
            chunkCount = chunkCount,
        )
    }

    private fun describeLanguage(code: LanguageCode): String = when (code) {
        LanguageCode.EN -> "English"
        LanguageCode.JA -> "Japanese"
        LanguageCode.ZH -> "Chinese"
        LanguageCode.KO -> "Korean"
        else -> code.name
    }

    companion object {
        private const val AVERAGE_TOKENS_PER_SENTENCE = 32
    }
}

private data class StructuredTranslationResponse(
    val translatedText: String,
    val sentencePairs: List<StructuredSentencePair>?,
    val metadata: StructuredTranslationMetadata? = null,
)

private data class StructuredSentencePair(
    val original: String,
    val translated: String,
)

private data class StructuredTranslationMetadata(
    val processingTimeMs: Long? = null,
    val tokenCount: Int? = null,
    val chunkCount: Int? = null,
)
