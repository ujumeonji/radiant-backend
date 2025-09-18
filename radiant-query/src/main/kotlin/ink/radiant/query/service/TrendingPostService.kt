package ink.radiant.query.service

import ink.radiant.core.domain.model.Post
import ink.radiant.query.entity.PostEntity
import ink.radiant.query.repository.PostRepository
import ink.radiant.query.repository.TrendingRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class TrendingPostService(
    private val trendingRepository: TrendingRepository,
    private val postRepository: PostRepository,
) {

    @Cacheable("trending-posts", key = "#limit")
    fun findTrendingPosts(limit: Int = 10): List<Post> {
        val validLimit = limit.coerceIn(1, 50)
        val sevenDaysAgo = OffsetDateTime.now().minusDays(7)
        val pageable = PageRequest.of(0, validLimit)

        val trendingEntities = trendingRepository.findTrendingPostsSince(sevenDaysAgo, pageable)

        val postIds = trendingEntities.map { it.postId }
        if (postIds.isEmpty()) {
            return emptyList()
        }

        val posts = postRepository.findAllById(postIds)
            .filter { !it.isDeleted() }
            .associateBy { it.id }

        return trendingEntities.mapNotNull { trending ->
            posts[trending.postId]?.toDomainModel()
        }
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
