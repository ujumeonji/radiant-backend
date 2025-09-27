package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.command.RequestTranslationCommand
import ink.radiant.command.service.TranslationCommandService
import ink.radiant.core.domain.model.Language
import ink.radiant.core.domain.model.LanguageCode
import ink.radiant.core.domain.model.TranslationSessionId
import ink.radiant.query.service.TranslationQueryService
import ink.radiant.web.codegen.types.TranslationInput
import ink.radiant.web.codegen.types.TranslationOptions
import ink.radiant.web.codegen.types.TranslationQueued
import ink.radiant.web.codegen.types.TranslationSession
import ink.radiant.web.codegen.types.TranslationStatus
import ink.radiant.web.security.AdminAuthorizationService
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@DgsComponent
class TranslationDataFetcher(
    private val translationCommandService: TranslationCommandService,
    private val translationQueryService: TranslationQueryService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    @DgsMutation
    fun translateToKorean(@InputArgument input: TranslationInput): TranslationQueued {
        val userId = adminAuthorizationService.currentAdminUserId()
        val command = toCommand(input, userId)
        return translationCommandService.requestTranslation(command)
            .toGraphQLQueued()
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

    private fun TranslationSessionId.toGraphQLQueued(): TranslationQueued {
        return TranslationQueued(
            sessionId = value,
            status = TranslationStatus.IN_PROGRESS,
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

    private fun Instant.toOffsetDateTime(): OffsetDateTime {
        return OffsetDateTime.ofInstant(this, ZoneOffset.UTC)
    }
}
