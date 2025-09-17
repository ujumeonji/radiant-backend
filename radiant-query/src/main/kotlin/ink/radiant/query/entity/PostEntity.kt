package ink.radiant.query.entity

import jakarta.persistence.*

@Entity
@Table(name = "posts")
class PostEntity(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "body", columnDefinition = "TEXT")
    val body: String? = null,

    @Column(name = "translated_title")
    val translatedTitle: String? = null,

    @Column(name = "original_sentences")
    val originalSentences: String = "",

    @Column(name = "translated_sentences")
    val translatedSentences: String = "",

    @Column(name = "likes", nullable = false)
    val likes: Int = 0,

    @Column(name = "comments_count", nullable = false)
    val commentsCount: Int = 0,

    @Column(name = "thumbnail_url")
    val thumbnailUrl: String? = null,

    @Column(name = "author_id")
    val authorId: String? = null,
) : BaseEntity() {

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
