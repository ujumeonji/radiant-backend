package ink.radiant.infrastructure.view

import java.time.OffsetDateTime

data class TrendingQueryModel(
    val postId: String,
    val viewCount: Long,
    val trendScore: Double,
    val lastViewedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val deletedAt: OffsetDateTime? = null,
)
