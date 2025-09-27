package ink.radiant.command.subscriber

import ink.radiant.command.service.TranslationProcessingService
import ink.radiant.core.domain.event.TranslationRequestedEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TranslationRequestedEventSubscriber(
    private val translationProcessingService: TranslationProcessingService,
) {

    @Async(value = "translationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onTranslationRequested(event: TranslationRequestedEvent) = try {
        log.info("Scheduling translation for session {}", event.sessionId.value)
        translationProcessingService.processTranslation(event)
        log.info("Translation request processed for session {}", event.sessionId.value)
    } catch (exception: Exception) {
        log.error("Failed to process translation for session {}", event.sessionId.value, exception)
    }

    companion object {
        private val log = LoggerFactory.getLogger(TranslationRequestedEventSubscriber::class.java)
    }
}
