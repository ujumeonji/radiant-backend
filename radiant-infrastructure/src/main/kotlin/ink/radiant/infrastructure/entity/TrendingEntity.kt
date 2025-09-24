package ink.radiant.infrastructure.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

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
    @Column(name = "id")
    var id: String = UUID.randomUUID().toString(),

    @Column(name = "post_id")
    var postId: String,

    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0,

    @Column(name = "trend_score", nullable = false)
    var trendScore: Double = 0.0,

    @Column(name = "last_viewed_at")
    var lastViewedAt: OffsetDateTime? = null,
) : BaseEntity() {

    constructor() : this(
        postId = "",
    )

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

    companion object {

        fun create(postId: String): TrendingEntity {
            return TrendingEntity(
                postId = postId,
            )
        }
    }
}
