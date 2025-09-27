package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.command.RequestTranslationCommand
import ink.radiant.command.service.TranslationCommandService
import ink.radiant.core.domain.exception.TranslationException
import ink.radiant.core.domain.model.Language
import ink.radiant.core.domain.model.LanguageCode
import ink.radiant.core.domain.model.SentencePair
import ink.radiant.core.domain.model.TranslationMetadata
import ink.radiant.core.domain.model.TranslationResult
import ink.radiant.query.service.TranslationQueryService
import ink.radiant.web.codegen.types.TranslationError
import ink.radiant.web.codegen.types.TranslationErrorType
import ink.radiant.web.codegen.types.TranslationInput
import ink.radiant.web.codegen.types.TranslationOptions
import ink.radiant.web.codegen.types.TranslationSession
import ink.radiant.web.codegen.types.TranslationStatus
import ink.radiant.web.security.AdminAuthorizationService
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.CompletableFuture

@DgsComponent
class TranslationDataFetcher(
    private val translationCommandService: TranslationCommandService,
    private val translationQueryService: TranslationQueryService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    @DgsMutation
    fun translateToKorean(@InputArgument input: TranslationInput): CompletableFuture<Any> {
        val userId = adminAuthorizationService.currentAdminUserId()
        return try {
            val command = toCommand(input, userId)

            translationCommandService.requestTranslation(command)
                .thenApply<Any> { result -> result.toGraphQL() }
                .exceptionally { throwable -> toError(throwable) }
        } catch (exception: TranslationException) {
            CompletableFuture.completedFuture(toError(exception))
        }
    }

    @DgsQuery
    fun translationSession(@InputArgument sessionId: String): TranslationSession? {
        adminAuthorizationService.ensureAdmin()
        val session = translationQueryService.getTranslationSession(sessionId) ?: return null
        return session.toGraphQLSession()
    }

    private fun toCommand(input: TranslationInput, userId: String): RequestTranslationCommand {
        val options: TranslationOptions? = input.options
        val preserveFormatting = options?.preserveFormatting ?: true

        return RequestTranslationCommand(
            userId = userId,
            sourceText = input.sourceText,
            sourceLanguageHint = input.sourceLanguage?.let { graphCode ->
                LanguageCode.valueOf(graphCode.name)
            },
            preserveFormatting = preserveFormatting,
            targetLanguage = LanguageCode.KO,
        )
    }

    private fun TranslationResult.toGraphQL(): ink.radiant.web.codegen.types.TranslationResult {
        return ink.radiant.web.codegen.types.TranslationResult(
            sessionId = sessionId.value,
            sourceLanguage = sourceLanguage.toGraphQL(),
            translatedText = translatedText,
            sentencePairs = sentencePairs.map { pair -> pair.toGraphQL() },
            metadata = metadata.toGraphQL(),
        )
    }

    private fun SentencePair.toGraphQL(): ink.radiant.web.codegen.types.SentencePair {
        return ink.radiant.web.codegen.types.SentencePair(
            order = order,
            original = original,
            translated = translated,
        )
    }

    private fun TranslationMetadata.toGraphQL(): ink.radiant.web.codegen.types.TranslationMetadata {
        return ink.radiant.web.codegen.types.TranslationMetadata(
            processingTimeMs = processingTimeMs.toInt(),
            tokenCount = tokenCount,
            chunkCount = chunkCount,
        )
    }

    private fun Language.toGraphQL(): ink.radiant.web.codegen.types.Language {
        return ink.radiant.web.codegen.types.Language(
            code = ink.radiant.web.codegen.types.LanguageCode.valueOf(code.name),
            name = name,
            confidence = confidence,
        )
    }

    private fun ink.radiant.core.domain.model.TranslationSession.toGraphQLSession(): TranslationSession {
        return TranslationSession(
            id = id.value,
            userId = userId,
            sourceLanguage = sourceLanguage.toGraphQL(),
            textLength = textLength,
            status = TranslationStatus.valueOf(status.name),
            createdAt = createdAt.toOffsetDateTime(),
            completedAt = completedAt?.toOffsetDateTime(),
        )
    }

    private fun toError(throwable: Throwable): TranslationError {
        val cause = unwrapThrowable(throwable)
        return when (cause) {
            is TranslationException -> TranslationError(
                type = cause.errorType.toGraphQLErrorType(),
                message = cause.message ?: GENERIC_ERROR_MESSAGE,
                code = cause.errorType.name,
                sessionId = null,
            )
            else -> TranslationError(
                type = TranslationErrorType.UNKNOWN_ERROR,
                message = cause.message ?: GENERIC_ERROR_MESSAGE,
                code = ink.radiant.core.domain.model.TranslationErrorType.UNKNOWN_ERROR.name,
                sessionId = null,
            )
        }
    }

    private fun unwrapThrowable(throwable: Throwable): Throwable {
        var current = throwable
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current
    }

    private fun ink.radiant.core.domain.model.TranslationErrorType.toGraphQLErrorType(): TranslationErrorType {
        return TranslationErrorType.valueOf(name)
    }

    private fun Instant.toOffsetDateTime(): OffsetDateTime {
        return OffsetDateTime.ofInstant(this, ZoneOffset.UTC)
    }

    companion object {
        private const val GENERIC_ERROR_MESSAGE = "Translation request failed"
    }
}
