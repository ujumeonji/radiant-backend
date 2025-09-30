package ink.radiant.command.subscriber

import ink.radiant.core.domain.entity.TrendingEntity
import ink.radiant.core.domain.event.DomainEvent
import ink.radiant.core.domain.event.PostViewedEvent
import ink.radiant.infrastructure.messaging.EventSubscriber
import ink.radiant.infrastructure.repository.TrendingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.sqrt

@Component
class PostViewedEventSubscriber(
    private val trendingRepository: TrendingRepository,
) : EventSubscriber() {

    @Transactional
    override fun onDomainEvent(event: DomainEvent) {
        if (event !is PostViewedEvent) {
            return
        }

        try {
            val trending = trendingRepository.findByPostId(event.postId)
                ?: createNewTrendingEntity(event.postId)

            trending.incrementView()

            val newScore = calculateScore(trending)
            trending.updateTrendScore(newScore)

            trendingRepository.save(trending)
        } catch (exception: Exception) {
            logger.error("Failed to update trending score for post {}", event.postId, exception)
        }
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
        val ageInHours = ChronoUnit.HOURS.between(createdAt, OffsetDateTime.now()).toDouble()

        if (ageInHours > MAX_AGE_IN_HOURS) {
            return ZERO_SCORE
        }

        return BASE_SCORE / sqrt(max(ageInHours + MIN_AGE_IN_HOURS, MIN_AGE_IN_HOURS.toDouble()))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PostViewedEventSubscriber::class.java)
        private const val MAX_AGE_IN_HOURS = 168
        private const val MIN_AGE_IN_HOURS = 1
        private const val BASE_SCORE = 1.0
        private const val ZERO_SCORE = 0.0
    }
}
