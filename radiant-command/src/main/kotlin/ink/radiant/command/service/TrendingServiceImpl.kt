package ink.radiant.command.service

import ink.radiant.core.domain.model.Post
import ink.radiant.infrastructure.mapper.TrendingQueryMapper
import ink.radiant.query.service.PostQueryService
import ink.radiant.query.service.TrendingQueryService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional
class TrendingServiceImpl(
    private val trendingQueryMapper: TrendingQueryMapper,
    private val postQueryService: PostQueryService,
) : TrendingQueryService {

    @Cacheable("trending-posts", key = "#limit")
    override fun findTrendingPosts(limit: Int): List<Post> {
        val validLimit = limit.coerceIn(1, 50)
        val sevenDaysAgo = OffsetDateTime.now().minusDays(7)

        val trendingModels = trendingQueryMapper.findTrendingPostsSince(sevenDaysAgo, validLimit)

        val postIdStrings = trendingModels.map { it.postId }
        if (postIdStrings.isEmpty()) {
            return emptyList()
        }

        val posts = postQueryService.findPostsByIds(postIdStrings)

        val postMap = posts.associateBy { it.id.toString() }
        return postIdStrings.mapNotNull { postId -> postMap[postId] }
    }
}
