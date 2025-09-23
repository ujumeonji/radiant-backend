package ink.radiant.command.entity

import ink.radiant.core.domain.model.Post
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "posts")
class PostEntity {
    @Id
    @Column(name = "id")
    lateinit var id: String

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @Column(name = "body", columnDefinition = "TEXT")
    var body: String? = null

    @Column(name = "translated_title")
    var translatedTitle: String? = null

    @Column(name = "original_sentences", columnDefinition = "TEXT")
    var originalSentences: String = ""

    @Column(name = "translated_sentences", columnDefinition = "TEXT")
    var translatedSentences: String = ""

    @Column(name = "likes", nullable = false)
    var likes: Int = 0

    @Column(name = "comments_count", nullable = false)
    var commentsCount: Int = 0

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null

    @Column(name = "author_id")
    var authorId: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null

    constructor()

    constructor(
        id: String,
        title: String,
        body: String? = null,
        translatedTitle: String? = null,
        originalSentences: String = "",
        translatedSentences: String = "",
        likes: Int = 0,
        commentsCount: Int = 0,
        thumbnailUrl: String? = null,
        authorId: String? = null,
        createdAt: OffsetDateTime = OffsetDateTime.now(),
        updatedAt: OffsetDateTime = OffsetDateTime.now(),
        deletedAt: OffsetDateTime? = null,
    ) {
        this.id = id
        this.title = title
        this.body = body
        this.translatedTitle = translatedTitle
        this.originalSentences = originalSentences
        this.translatedSentences = translatedSentences
        this.likes = likes
        this.commentsCount = commentsCount
        this.thumbnailUrl = thumbnailUrl
        this.authorId = authorId
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.deletedAt = deletedAt
    }

    fun updateFrom(post: Post) {
        this.title = post.title
        this.body = post.body
        this.translatedTitle = post.translatedTitle
        this.originalSentences = post.originalSentences.joinToString("\n")
        this.translatedSentences = post.translatedSentences.joinToString("\n")
        this.likes = post.likes
        this.commentsCount = post.commentsCount
        this.thumbnailUrl = post.thumbnailUrl
        this.authorId = post.authorId
        this.updatedAt = OffsetDateTime.now()
    }

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

    fun markAsDeleted() {
        this.deletedAt = OffsetDateTime.now()
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

    companion object {
        fun fromDomainModel(post: Post): PostEntity {
            return PostEntity(
                id = post.id,
                title = post.title,
                body = post.body,
                translatedTitle = post.translatedTitle,
                originalSentences = post.originalSentences.joinToString("\n"),
                translatedSentences = post.translatedSentences.joinToString("\n"),
                likes = post.likes,
                commentsCount = post.commentsCount,
                thumbnailUrl = post.thumbnailUrl,
                authorId = post.authorId,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                deletedAt = post.deletedAt,
            )
        }
    }
}
