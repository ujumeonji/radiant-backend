package ink.radiant.query.model

import java.time.OffsetDateTime

data class TrendingQueryModel(
    val postId: String,
    val viewCount: Long,
    val trendScore: Double,
    val lastViewedAt: OffsetDateTime?,
)
