package ink.radiant.command.service

import ink.radiant.command.RequestTranslationCommand
import ink.radiant.core.domain.model.TranslationResult
import java.util.concurrent.CompletableFuture

interface TranslationCommandService {
    fun requestTranslation(command: RequestTranslationCommand): CompletableFuture<TranslationResult>
}
