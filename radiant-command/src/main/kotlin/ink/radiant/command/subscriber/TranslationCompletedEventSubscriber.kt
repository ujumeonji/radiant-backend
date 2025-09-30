package ink.radiant.command.subscriber

import ink.radiant.core.domain.entity.PostEntity
import ink.radiant.core.domain.event.DomainEvent
import ink.radiant.core.domain.event.TranslationCompletedEvent
import ink.radiant.infrastructure.messaging.EventSubscriber
import ink.radiant.infrastructure.repository.PostRepository
import ink.radiant.infrastructure.repository.TranslationSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TranslationCompletedEventSubscriber(
    private val postRepository: PostRepository,
    private val translationSessionRepository: TranslationSessionRepository,
) : EventSubscriber() {

    @Transactional
    override fun onDomainEvent(event: DomainEvent) {
        if (event !is TranslationCompletedEvent) {
            return
        }
        handleTranslationCompleted(event)
    }

    private fun handleTranslationCompleted(event: TranslationCompletedEvent) {
        val sessionId = event.sessionId.value
        val session = translationSessionRepository.findById(sessionId).orElse(null)
        if (session == null) {
            logger.warn("Translation session {} not found while handling completion", sessionId)
            return
        }

        val post = PostEntity.fromTranslation(
            translatedText = event.translatedText,
            sentencePairs = event.sentencePairs,
            authorId = session.userId,
        )

        postRepository.save(post)
        logger.info("Persisted translated post for session {}", sessionId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TranslationCompletedEventSubscriber::class.java)
    }
}
