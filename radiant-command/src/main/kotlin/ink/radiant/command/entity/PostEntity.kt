package ink.radiant.command.entity

import ink.radiant.infrastructure.share.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "posts")
class PostEntity(
    @Id
    @Column(name = "id")
    var id: String,

    @Column(name = "title", nullable = false)
    var title: String,

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

    fun markAsDeleted() {
        this.softDelete()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
