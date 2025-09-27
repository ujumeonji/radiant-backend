package ink.radiant.infrastructure.repository

import ink.radiant.infrastructure.entity.TranslationSessionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TranslationSessionRepository : JpaRepository<TranslationSessionEntity, String>
