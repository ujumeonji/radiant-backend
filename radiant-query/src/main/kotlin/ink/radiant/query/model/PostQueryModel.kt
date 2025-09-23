package ink.radiant.query.model

import ink.radiant.core.domain.model.Post
import java.time.OffsetDateTime

data class PostQueryModel(
    val id: String,
    val title: String,
    val body: String?,
    val translatedTitle: String?,
    val originalSentences: String,
    val translatedSentences: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val likes: Int,
    val commentsCount: Int,
    val thumbnailUrl: String?,
    val authorId: String?,
    val deletedAt: OffsetDateTime? = null,
) {
    fun toDomainModel(): Post {
        return Post(
            id = this.id,
            title = this.title,
            body = this.body,
            translatedTitle = this.translatedTitle,
            originalSentences = this.originalSentences.split("\n").filter { it.isNotBlank() },
            translatedSentences = this.translatedSentences.split("\n").filter { it.isNotBlank() },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            likes = this.likes,
            commentsCount = this.commentsCount,
            thumbnailUrl = this.thumbnailUrl,
            authorId = this.authorId,
            deletedAt = this.deletedAt,
        )
    }
}
