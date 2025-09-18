package ink.radiant.query.service

import ink.radiant.core.domain.event.PostViewedEvent
import ink.radiant.core.domain.model.PageInfo
import ink.radiant.core.domain.model.Post
import ink.radiant.core.domain.model.PostConnection
import ink.radiant.core.domain.model.PostEdge
import ink.radiant.infrastructure.messaging.EventPublisher
import ink.radiant.query.entity.PostEntity
import ink.radiant.query.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class PostQueryService(
    private val postRepository: PostRepository,
    private val eventPublisher: EventPublisher,
) : QueryService() {

    fun findPosts(first: Int?, after: String?): PostConnection {
        val limit = (first ?: 10).coerceIn(1, 100)
        val pageable = PageRequest.of(0, limit + 1)

        val page: Page<PostEntity> = if (after != null) {
            val cursor = decodeCursor(after)
            postRepository.findAllAfterCursor(cursor, pageable)
        } else {
            postRepository.findAllOrderByCreatedAtDesc(pageable)
        }

        val posts = page.content
        val hasNextPage = posts.size > limit
        val resultPosts = if (hasNextPage) posts.dropLast(1) else posts

        val edges = resultPosts.map { postEntity ->
            PostEdge(
                post = postEntity.toDomainModel(),
                cursor = encodeCursor(
                    postEntity.createdAt ?: throw IllegalStateException("createdAt should not be null"),
                ),
            )
        }

        val pageInfo = PageInfo(
            hasNextPage = hasNextPage,
            hasPreviousPage = after != null,
            startCursor = edges.firstOrNull()?.cursor,
            endCursor = edges.lastOrNull()?.cursor,
        )

        return PostConnection(
            edges = edges,
            pageInfo = pageInfo,
        )
    }

    fun findPostById(id: String): Post? {
        val post = postRepository.findByIdAndNotDeleted(id)?.toDomainModel()

        if (post != null) {
            val event = PostViewedEvent(
                aggregateId = id,
                postId = id,
            )
            eventPublisher.publish(event)
        }

        return post
    }

    private fun encodeCursor(dateTime: OffsetDateTime): String {
        return Base64.getEncoder().encodeToString(dateTime.toString().toByteArray())
    }

    private fun decodeCursor(cursor: String): OffsetDateTime {
        val decoded = String(Base64.getDecoder().decode(cursor))
        return OffsetDateTime.parse(decoded)
    }

    private fun PostEntity.toDomainModel(): Post {
        return Post(
            id = this.id,
            title = this.title,
            body = this.body,
            translatedTitle = this.translatedTitle,
            originalSentences = this.originalSentences.split("\n").filter { it.isNotBlank() },
            translatedSentences = this.translatedSentences.split("\n").filter { it.isNotBlank() },
            createdAt = this.createdAt ?: throw IllegalStateException("createdAt should not be null"),
            updatedAt = this.updatedAt ?: throw IllegalStateException("updatedAt should not be null"),
            likes = this.likes,
            commentsCount = this.commentsCount,
            thumbnailUrl = this.thumbnailUrl,
            authorId = this.authorId,
            deletedAt = this.deletedAt,
        )
    }
}
