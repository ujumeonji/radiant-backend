package ink.radiant.command.service

import ink.radiant.core.domain.event.PostViewedEvent
import ink.radiant.core.domain.model.PageInfo
import ink.radiant.core.domain.model.Post
import ink.radiant.core.domain.model.PostConnection
import ink.radiant.core.domain.model.PostEdge
import ink.radiant.infrastructure.mapper.PostQueryMapper
import ink.radiant.infrastructure.messaging.EventPublisher
import ink.radiant.query.service.PostQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.Base64

@Service
@Transactional
class PostServiceImpl(
    private val postQueryMapper: PostQueryMapper,
    private val eventPublisher: EventPublisher,
) : PostCommandService, PostQueryService {

    override fun findPosts(first: Int?, after: String?): PostConnection {
        val limit = (first ?: 10).coerceIn(1, 100)
        val actualLimit = limit + 1

        val posts = if (after != null) {
            val cursor = decodeCursor(after)
            postQueryMapper.findAllAfterCursor(cursor, actualLimit)
        } else {
            postQueryMapper.findAllOrderByCreatedAtDesc(actualLimit)
        }

        val hasNextPage = posts.size > limit
        val resultPosts = if (hasNextPage) posts.dropLast(1) else posts

        val edges = resultPosts.map { postQueryModel ->
            PostEdge(
                post = postQueryModel.toDomainModel(),
                cursor = encodeCursor(postQueryModel.createdAt),
            )
        }

        val pageInfo = PageInfo(
            hasNextPage = hasNextPage,
            hasPreviousPage = after != null,
            startCursor = edges.firstOrNull()?.cursor,
            endCursor = edges.lastOrNull()?.cursor,
        )

        val totalCount = postQueryMapper.countExistingPosts().toInt()

        return PostConnection(
            edges = edges,
            pageInfo = pageInfo,
            totalCount = totalCount,
        )
    }

    override fun findPostById(id: String): Post? {
        val post = postQueryMapper.findByIdAndNotDeleted(id)?.toDomainModel()

        if (post != null) {
            val event = PostViewedEvent(
                aggregateId = id,
                postId = id,
            )
            eventPublisher.publish(event)
        }

        return post
    }

    override fun findPostsByIds(ids: List<String>): List<Post> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        return postQueryMapper.findByIdsAndNotDeleted(ids)
            .map { it.toDomainModel() }
    }

    private fun encodeCursor(dateTime: OffsetDateTime): String {
        return Base64.getEncoder().encodeToString(dateTime.toString().toByteArray())
    }

    private fun decodeCursor(cursor: String): OffsetDateTime {
        val decoded = String(Base64.getDecoder().decode(cursor))
        return OffsetDateTime.parse(decoded)
    }
}
