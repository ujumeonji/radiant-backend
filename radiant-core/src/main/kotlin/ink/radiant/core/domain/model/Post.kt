package ink.radiant.core.domain.model

import java.time.OffsetDateTime

data class Post(
    val id: String,
    val title: String,
    val body: String?,
    val translatedTitle: String?,
    val originalSentences: List<String>,
    val translatedSentences: List<String>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val likes: Int,
    val commentsCount: Int,
    val thumbnailUrl: String?,
    val authorId: String?,
    val deletedAt: OffsetDateTime? = null,
) {
    fun isDeleted(): Boolean = deletedAt != null
}
