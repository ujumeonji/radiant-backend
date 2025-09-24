package ink.radiant.fixture

import ink.radiant.infrastructure.entity.TrendingEntity
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
                postId = "00000000-0000-0000-0000-000000000001",
                viewCount = 50,
                trendScore = 25.0,
                lastViewedAt = now.minusHours(1),
            ),
            createTrendingEntity(
                postId = "00000000-0000-0000-0000-000000000002",
                viewCount = 100,
                trendScore = 40.0,
                lastViewedAt = now.minusHours(2),
            ),
            createTrendingEntity(
                postId = "00000000-0000-0000-0000-000000000003",
                viewCount = 75,
                trendScore = 35.0,
                lastViewedAt = now.minusHours(3),
            ),
        )
    }
}
