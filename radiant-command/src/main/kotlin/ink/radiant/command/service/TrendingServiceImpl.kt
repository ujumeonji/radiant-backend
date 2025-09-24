package ink.radiant.command.service

import ink.radiant.core.domain.event.PostViewedEvent
import ink.radiant.core.domain.model.Post
import ink.radiant.infrastructure.entity.TrendingEntity
import ink.radiant.infrastructure.mapper.TrendingQueryMapper
import ink.radiant.infrastructure.repository.TrendingCommandRepository
import ink.radiant.query.service.PostQueryService
import ink.radiant.query.service.TrendingQueryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional
class TrendingServiceImpl(
    private val trendingCommandRepository: TrendingCommandRepository,
    private val trendingQueryMapper: TrendingQueryMapper,
    private val postQueryService: PostQueryService,
) : TrendingCommandService, TrendingQueryService {

    @EventListener
    override fun handlePostViewed(event: PostViewedEvent) {
        try {
            val trending = trendingCommandRepository.findByPostId(event.postId)
                ?: createNewTrendingEntity(event.postId)

            trending.incrementView()

            val newScore = calculateScore(trending)
            trending.updateTrendScore(newScore)

            trendingCommandRepository.save(trending)
        } catch (e: Exception) {
            log.error("Error handling post viewed event: ${e.message}", e)
        }
    }

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

    private fun createNewTrendingEntity(postId: String): TrendingEntity = TrendingEntity.create(
        postId = postId,
    )

    private fun calculateScore(trending: TrendingEntity): Double {
        val baseScore = trending.viewCount.toDouble() * BASE_SCORE
        val timeDecay = calculateTimeDecay(trending.createdAt ?: OffsetDateTime.now())

        return baseScore * timeDecay
    }

    private fun calculateTimeDecay(createdAt: OffsetDateTime): Double {
        val now = OffsetDateTime.now()
        val ageInHours = java.time.temporal.ChronoUnit.HOURS.between(createdAt, now).toDouble()

        if (ageInHours > MAX_AGE_IN_HOURS) {
            return ZERO_SCORE
        }

        return BASE_SCORE / kotlin.math.sqrt(
            kotlin.math.max(ageInHours + MIN_AGE_IN_HOURS, MIN_AGE_IN_HOURS.toDouble()),
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TrendingServiceImpl::class.java)
        private const val MAX_AGE_IN_HOURS = 168
        private const val MIN_AGE_IN_HOURS = 1
        private const val BASE_SCORE = 1.0
        private const val ZERO_SCORE = 0.0
    }
}
