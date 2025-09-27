package ink.radiant.command.service

import ink.radiant.command.RequestTranslationCommand
import ink.radiant.core.domain.model.TranslationSessionId

interface TranslationCommandService {
    fun requestTranslation(command: RequestTranslationCommand): TranslationSessionId
}
