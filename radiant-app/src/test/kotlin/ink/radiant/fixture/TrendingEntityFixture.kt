package ink.radiant.fixture

import ink.radiant.command.entity.TrendingEntity
import java.time.OffsetDateTime

object TrendingEntityFixture {

    fun createTrendingEntity(
        postId: String,
        viewCount: Long = 1,
        trendScore: Double = 1.0,
        lastViewedAt: OffsetDateTime = OffsetDateTime.now(),
    ): TrendingEntity {
        return TrendingEntity(
            postId = postId,
            viewCount = viewCount,
            trendScore = trendScore,
            lastViewedAt = lastViewedAt,
        )
    }

    fun createTrendingEntityList(): List<TrendingEntity> {
        val now = OffsetDateTime.now()
        return listOf(
            createTrendingEntity(
                postId = "post-1",
                viewCount = 50,
                trendScore = 25.0,
                lastViewedAt = now.minusHours(1),
            ),
            createTrendingEntity(
                postId = "post-2",
                viewCount = 100,
                trendScore = 40.0,
                lastViewedAt = now.minusHours(2),
            ),
            createTrendingEntity(
                postId = "post-3",
                viewCount = 75,
                trendScore = 35.0,
                lastViewedAt = now.minusHours(3),
            ),
        )
    }
}
