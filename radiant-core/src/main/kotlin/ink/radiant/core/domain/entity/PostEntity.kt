package ink.radiant.core.domain.entity

import ink.radiant.core.domain.aggregate.AggregateRoot
import ink.radiant.core.domain.event.PostCreatedEvent
import ink.radiant.core.domain.event.PostDeletedEvent
import ink.radiant.core.domain.event.PostLikedEvent
import ink.radiant.core.domain.event.PostUpdatedEvent
import ink.radiant.core.domain.model.SentencePair
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "posts")
class PostEntity(
    @Id
    @Column(name = "id")
    var id: String = UUID.randomUUID().toString(),

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
) : AggregateRoot() {

    constructor() : this(
        title = "",
    )

    fun updateTitle(newTitle: String, updatedBy: String) {
        require(newTitle.isNotBlank()) { "제목은 비어있을 수 없습니다." }
        require(newTitle.length <= 200) { "제목은 200자를 초과할 수 없습니다." }

        if (this.title != newTitle) {
            val oldTitle = this.title
            this.title = newTitle

            applyEvent(
                PostUpdatedEvent(
                    aggregateId = this.id,
                    postId = this.id,
                    field = "title",
                    oldValue = oldTitle,
                    newValue = newTitle,
                    updatedBy = updatedBy,
                ),
            )
        }
    }

    fun updateBody(newBody: String?, updatedBy: String) {
        newBody?.let {
            require(it.length <= 10000) { "본문은 10,000자를 초과할 수 없습니다." }
        }

        if (this.body != newBody) {
            val oldBody = this.body
            this.body = newBody

            applyEvent(
                PostUpdatedEvent(
                    aggregateId = this.id,
                    postId = this.id,
                    field = "body",
                    oldValue = oldBody,
                    newValue = newBody,
                    updatedBy = updatedBy,
                ),
            )
        }
    }

    fun like(likedBy: String) {
        this.likes++

        applyEvent(
            PostLikedEvent(
                aggregateId = this.id,
                postId = this.id,
                likedBy = likedBy,
                totalLikes = this.likes,
            ),
        )
    }

    fun markAsDeleted(deletedBy: String?) {
        require(!isDeleted()) { "이미 삭제된 게시글입니다." }

        this.softDelete()

        applyEvent(
            PostDeletedEvent(
                aggregateId = this.id,
                postId = this.id,
                deletedBy = deletedBy,
            ),
        )
    }

    fun canBeEditedBy(userId: String): Boolean {
        return this.authorId == userId && !isDeleted()
    }

    fun canBeDeletedBy(userId: String): Boolean {
        return this.authorId == userId && !isDeleted()
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
        private const val LINE_SEPARATOR = "\n"
        private const val DEFAULT_TRANSLATED_TITLE = "번역된 게시글"

        fun create(title: String, body: String?, authorId: String, thumbnailUrl: String? = null): PostEntity {
            require(title.isNotBlank()) { "제목은 필수입니다." }
            require(title.length <= 200) { "제목은 200자를 초과할 수 없습니다." }
            require(authorId.isNotBlank()) { "작성자는 필수입니다." }
            body?.let {
                require(it.length <= 10000) { "본문은 10,000자를 초과할 수 없습니다." }
            }

            val postId = UUID.randomUUID().toString()
            val post = PostEntity(
                id = postId,
                title = title,
                body = body,
                likes = 0,
                commentsCount = 0,
                thumbnailUrl = thumbnailUrl,
                authorId = authorId,
            )

            post.applyEvent(
                PostCreatedEvent(
                    aggregateId = postId,
                    postId = postId,
                    title = title,
                    body = body,
                    authorId = authorId,
                    thumbnailUrl = thumbnailUrl,
                ),
            )

            return post
        }

        fun fromTranslation(translatedText: String, sentencePairs: List<SentencePair>, authorId: String): PostEntity {
            val ordered = sentencePairs.sortedBy { it.order }
            val translatedTitle = ordered.firstOrNull()?.translated?.takeIf { it.isNotBlank() }
                ?: DEFAULT_TRANSLATED_TITLE
            val originalTitle = ordered.firstOrNull()?.original?.takeIf { it.isNotBlank() }

            val post = PostEntity(
                title = translatedTitle,
                body = translatedText,
                translatedTitle = originalTitle,
                originalSentences = ordered.joinToString(separator = LINE_SEPARATOR) { it.original.trim() },
                translatedSentences = ordered.joinToString(separator = LINE_SEPARATOR) { it.translated.trim() },
                likes = 0,
                commentsCount = 0,
                thumbnailUrl = null,
                authorId = authorId,
            )

            post.applyEvent(
                PostCreatedEvent(
                    aggregateId = post.id,
                    postId = post.id,
                    title = translatedTitle,
                    body = translatedText,
                    authorId = authorId,
                ),
            )

            return post
        }
    }
}
