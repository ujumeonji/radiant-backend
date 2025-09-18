package ink.radiant.query.service

import ink.radiant.query.entity.PostEntity
import ink.radiant.query.entity.TrendingEntity
import ink.radiant.query.repository.PostRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.sqrt

@Component
class TrendingScoreCalculator(
    private val postRepository: PostRepository,
) {

    fun calculateScore(trending: TrendingEntity): Double {
        val post = postRepository.findByIdAndNotDeleted(trending.postId) ?: return ZERO_SCORE

        val baseScore = trending.viewCount.toDouble() * BASE_SCORE
        val timeDecay = calculateTimeDecay(post)

        return baseScore * timeDecay
    }

    private fun calculateTimeDecay(post: PostEntity): Double {
        val createdAt = post.createdAt ?: return ZERO_SCORE
        val now = OffsetDateTime.now()
        val ageInHours = ChronoUnit.HOURS.between(createdAt, now).toDouble()

        if (ageInHours > MAX_AGE_IN_HOURS) {
            return ZERO_SCORE
        }

        return BASE_SCORE / sqrt(max(ageInHours + MIN_AGE_IN_HOURS, MIN_AGE_IN_HOURS.toDouble()))
    }

    companion object {
        private const val MAX_AGE_IN_HOURS = 168
        private const val MIN_AGE_IN_HOURS = 1
        private const val BASE_SCORE = 1.0
        private const val ZERO_SCORE = 0.0
    }
}
