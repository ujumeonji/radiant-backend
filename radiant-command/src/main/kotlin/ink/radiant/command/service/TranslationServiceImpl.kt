package ink.radiant.command.service

import ink.radiant.command.RequestTranslationCommand
import ink.radiant.core.domain.event.TranslationCompletedEvent
import ink.radiant.core.domain.event.TranslationFailedEvent
import ink.radiant.core.domain.event.TranslationRequestedEvent
import ink.radiant.core.domain.exception.TranslationException
import ink.radiant.core.domain.exception.TranslationUnknownException
import ink.radiant.core.domain.model.Language
import ink.radiant.core.domain.model.LanguageCode
import ink.radiant.core.domain.model.SentencePair
import ink.radiant.core.domain.model.TranslationErrorType
import ink.radiant.core.domain.model.TranslationMetadata
import ink.radiant.core.domain.model.TranslationResult
import ink.radiant.core.domain.model.TranslationSession
import ink.radiant.core.domain.model.TranslationSessionId
import ink.radiant.infrastructure.ai.LanguageDetectionService
import ink.radiant.infrastructure.ai.TranslationModelClient
import ink.radiant.infrastructure.ai.TranslationModelRequest
import ink.radiant.infrastructure.ai.TranslationProviderException
import ink.radiant.infrastructure.entity.TranslationSessionEntity
import ink.radiant.infrastructure.entity.toDomainSession
import ink.radiant.infrastructure.entity.toResult
import ink.radiant.infrastructure.messaging.EventPublisher
import ink.radiant.infrastructure.repository.TranslationSessionRepository
import ink.radiant.query.service.TranslationQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionOperations
import java.time.Instant

@Service
@Transactional
class TranslationServiceImpl(
    private val eventPublisher: EventPublisher,
    private val languageDetectionService: LanguageDetectionService,
    private val translationModelClient: TranslationModelClient,
    private val translationSessionRepository: TranslationSessionRepository,
    private val transactionTemplate: TransactionOperations,
) : TranslationCommandService, TranslationQueryService, TranslationProcessingService {

    private val log = LoggerFactory.getLogger(TranslationServiceImpl::class.java)

    override fun requestTranslation(command: RequestTranslationCommand): TranslationSessionId {
        val sessionId = TranslationSessionId.generate()
        val detectedLanguage = languageDetectionService.detectLanguage(command.sourceText, command.sourceLanguageHint)
        val requestedAt = Instant.now()

        val translationSession = TranslationSessionEntity.create(
            sessionId = sessionId,
            userId = command.userId,
            language = detectedLanguage,
            sourceText = command.sourceText,
            requestedAt = requestedAt,
        )
        translationSessionRepository.save(translationSession)

        publishRequestedEvent(
            sessionId = translationSession.id,
            userId = command.userId,
            language = detectedLanguage,
            sourceText = command.sourceText,
            requestedAt = requestedAt,
            targetLanguage = command.targetLanguage,
            preserveFormatting = command.preserveFormatting,
        )

        return sessionId
    }

    override fun getTranslationSession(sessionId: String): TranslationSession? {
        return translationSessionRepository.findById(sessionId)
            .map { entity -> entity.toDomainSession() }
            .orElse(null)
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    override fun processTranslation(event: TranslationRequestedEvent) {
        val sessionId = event.sessionId
        log.info("Translating session {}", sessionId.value)
        try {
            val response = translationModelClient.translate(
                TranslationModelRequest(
                    sessionId = sessionId.value,
                    sourceLanguage = event.sourceLanguage.code,
                    targetLanguage = event.targetLanguage,
                    sourceText = event.sourceText,
                    preserveFormatting = event.preserveFormatting,
                ),
            )

            transactionTemplate.executeWithoutResult {
                markCompletion(
                    sessionId = sessionId,
                    translatedText = response.translatedText,
                    pairs = response.sentencePairs,
                    metadata = response.metadata,
                    completedAt = Instant.now(),
                )
            }
            log.info("Translation completed for session {}", sessionId.value)
        } catch (exception: TranslationProviderException) {
            val translationException = exception.toTranslationException()
            transactionTemplate.executeWithoutResult {
                markFailure(sessionId, translationException)
            }
            log.warn("Translation provider failure for session {}", sessionId.value, translationException)
        } catch (exception: TranslationException) {
            transactionTemplate.executeWithoutResult {
                markFailure(sessionId, exception)
            }
            log.warn("Translation failure for session {}", sessionId.value, exception)
        } catch (exception: Exception) {
            val failure = TranslationUnknownException(cause = exception)
            transactionTemplate.executeWithoutResult {
                markFailure(sessionId, failure)
            }
            log.error("Unexpected translation error for session {}", sessionId.value, failure)
        }
    }

    private fun markCompletion(
        sessionId: TranslationSessionId,
        translatedText: String,
        pairs: List<SentencePair>,
        metadata: TranslationMetadata,
        completedAt: Instant,
    ): TranslationResult {
        val session = loadSession(sessionId.value)
        session.markCompleted(
            translatedText = translatedText,
            sentencePairs = pairs,
            metadata = metadata,
            completedAt = completedAt,
        )
        translationSessionRepository.save(session)

        publishCompletedEvent(
            sessionId = sessionId,
            translatedText = translatedText,
            sentencePairs = pairs,
            metadata = metadata,
            completedAt = completedAt,
        )

        return session.toResult()
            ?: throw IllegalStateException("Translation result missing for session ${sessionId.value}")
    }

    private fun markFailure(sessionId: TranslationSessionId, exception: TranslationException) {
        val session = loadSession(sessionId.value)
        session.markFailed(
            errorType = exception.errorType,
            errorMessage = exception.message ?: DEFAULT_FAILURE_MESSAGE,
            failedAt = Instant.now(),
        )
        translationSessionRepository.save(session)

        publishFailedEvent(
            sessionId = sessionId,
            errorType = exception.errorType,
            errorMessage = exception.message ?: DEFAULT_FAILURE_MESSAGE,
        )
    }

    private fun loadSession(sessionId: String): TranslationSessionEntity {
        return translationSessionRepository.findById(sessionId)
            .orElseThrow { IllegalStateException("Translation session $sessionId not found") }
    }

    private fun publishRequestedEvent(
        sessionId: String,
        userId: String,
        language: Language,
        sourceText: String,
        requestedAt: Instant,
        targetLanguage: LanguageCode,
        preserveFormatting: Boolean,
    ) {
        val event = TranslationRequestedEvent(
            sessionId = TranslationSessionId.from(sessionId),
            userId = userId,
            sourceLanguage = language,
            sourceText = sourceText,
            textLength = sourceText.length,
            requestedAt = requestedAt,
            targetLanguage = targetLanguage,
            preserveFormatting = preserveFormatting,
        )
        eventPublisher.publish(event)
    }

    private fun publishCompletedEvent(
        sessionId: TranslationSessionId,
        translatedText: String,
        sentencePairs: List<SentencePair>,
        metadata: TranslationMetadata,
        completedAt: Instant,
    ) {
        val event = TranslationCompletedEvent(
            sessionId = sessionId,
            translatedText = translatedText,
            sentencePairs = sentencePairs,
            resultMetadata = metadata,
            completedAt = completedAt,
        )
        eventPublisher.publish(event)
    }

    private fun publishFailedEvent(
        sessionId: TranslationSessionId,
        errorType: TranslationErrorType,
        errorMessage: String,
    ) {
        val event = TranslationFailedEvent(
            sessionId = sessionId,
            errorType = errorType,
            errorMessage = errorMessage,
        )
        eventPublisher.publish(event)
    }

    companion object {
        private const val DEFAULT_FAILURE_MESSAGE = "Translation request failed"
    }
}

private fun TranslationProviderException.toTranslationException(): TranslationException {
    val resolvedMessage = message ?: "Translation failed"
    return TranslationException(errorType, resolvedMessage, cause)
}
