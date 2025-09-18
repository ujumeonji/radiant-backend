package ink.radiant.query.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "trending",
    indexes = [
        Index(name = "idx_trend_score", columnList = "trendScore DESC"),
        Index(name = "idx_last_viewed", columnList = "lastViewedAt DESC"),
    ],
)
class TrendingEntity(
    @Id
    @Column(name = "post_id")
    val postId: String,

    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0,

    @Column(name = "trend_score", nullable = false)
    var trendScore: Double = 0.0,

    @Column(name = "last_viewed_at")
    var lastViewedAt: OffsetDateTime? = null,
) : BaseEntity() {

    fun incrementView() {
        viewCount++
        lastViewedAt = OffsetDateTime.now()
    }

    fun updateTrendScore(score: Double) {
        trendScore = score
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrendingEntity

        return postId == other.postId
    }

    override fun hashCode(): Int {
        return postId.hashCode()
    }
}
