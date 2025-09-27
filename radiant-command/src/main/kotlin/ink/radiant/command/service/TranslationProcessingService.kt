package ink.radiant.command.service

import ink.radiant.core.domain.event.TranslationRequestedEvent

interface TranslationProcessingService {
    fun processTranslation(event: TranslationRequestedEvent)
}
