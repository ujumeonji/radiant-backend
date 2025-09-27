package ink.radiant.infrastructure.entity

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
) : BaseEntity() {

    constructor() : this(
        title = "",
    )

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

    companion object {
        private const val LINE_SEPARATOR = "\n"
        private const val DEFAULT_TRANSLATED_TITLE = "번역된 게시글"

        fun fromTranslation(translatedText: String, sentencePairs: List<SentencePair>, authorId: String?): PostEntity {
            val ordered = sentencePairs.sortedBy { it.order }
            val translatedTitle = ordered.firstOrNull()?.translated?.takeIf { it.isNotBlank() }
                ?: DEFAULT_TRANSLATED_TITLE
            val originalTitle = ordered.firstOrNull()?.original?.takeIf { it.isNotBlank() }

            return PostEntity(
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
        }
    }
}
