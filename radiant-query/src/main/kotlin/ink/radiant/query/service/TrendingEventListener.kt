package ink.radiant.query.service

import ink.radiant.core.domain.event.PostViewedEvent
import ink.radiant.query.entity.TrendingEntity
import ink.radiant.query.repository.TrendingRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TrendingEventListener(
    private val trendingRepository: TrendingRepository,
    private val trendingScoreCalculator: TrendingScoreCalculator,
) {

    @EventListener
    @Transactional
    fun handlePostViewed(event: PostViewedEvent) {
        val trending = trendingRepository.findByPostIdAndNotDeleted(event.postId)
            ?: createNewTrendingEntity(event.postId)

        trending.incrementView()

        val newScore = trendingScoreCalculator.calculateScore(trending)
        trending.updateTrendScore(newScore)

        trendingRepository.save(trending)
    }

    private fun createNewTrendingEntity(postId: String): TrendingEntity {
        return TrendingEntity(postId = postId)
    }
}
