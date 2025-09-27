package ink.radiant.query.service

import ink.radiant.core.domain.model.TranslationSession

interface TranslationQueryService {
    fun getTranslationSession(sessionId: String): TranslationSession?
}
