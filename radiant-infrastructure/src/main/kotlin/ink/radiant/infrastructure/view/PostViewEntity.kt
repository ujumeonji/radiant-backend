package ink.radiant.infrastructure.view

import ink.radiant.core.domain.entity.BaseEntity
import ink.radiant.core.domain.entity.PostEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "post_views")
class PostViewEntity(
    @Id
    @Column(name = "id")
    var id: String = "",

    @Column(name = "title", nullable = false)
    var title: String = "",

    @Column(name = "body", columnDefinition = "TEXT")
    var body: String? = null,

    @Column(name = "translated_title")
    var translatedTitle: String? = null,

    @Column(name = "original_sentences", columnDefinition = "TEXT")
    var originalSentences: String = "",

    @Column(name = "translated_sentences", columnDefinition = "TEXT")
    var translatedSentences: String = "",

    @Column(name = "likes", nullable = false)
    var likes: Int = 0,

    @Column(name = "comments_count", nullable = false)
    var commentsCount: Int = 0,

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    @Column(name = "author_id")
    var authorId: String? = null,
) : BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostViewEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun fromPostEntity(postEntity: PostEntity): PostViewEntity {
            return PostViewEntity(
                id = postEntity.id,
                title = postEntity.title,
                body = postEntity.body,
                translatedTitle = postEntity.translatedTitle,
                originalSentences = postEntity.originalSentences,
                translatedSentences = postEntity.translatedSentences,
                likes = postEntity.likes,
                commentsCount = postEntity.commentsCount,
                thumbnailUrl = postEntity.thumbnailUrl,
                authorId = postEntity.authorId,
            )
        }
    }
}
