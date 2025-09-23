package ink.radiant.command.entity

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
class TrendingEntity {
    @Id
    @Column(name = "post_id")
    lateinit var postId: String

    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0

    @Column(name = "trend_score", nullable = false)
    var trendScore: Double = 0.0

    @Column(name = "last_viewed_at")
    var lastViewedAt: OffsetDateTime? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null

    constructor()

    constructor(
        postId: String,
        viewCount: Long = 0,
        trendScore: Double = 0.0,
        lastViewedAt: OffsetDateTime? = null,
        createdAt: OffsetDateTime = OffsetDateTime.now(),
        updatedAt: OffsetDateTime = OffsetDateTime.now(),
        deletedAt: OffsetDateTime? = null,
    ) {
        this.postId = postId
        this.viewCount = viewCount
        this.trendScore = trendScore
        this.lastViewedAt = lastViewedAt
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.deletedAt = deletedAt
    }

    fun incrementView() {
        viewCount++
        lastViewedAt = OffsetDateTime.now()
        updatedAt = OffsetDateTime.now()
    }

    fun updateTrendScore(score: Double) {
        trendScore = score
        updatedAt = OffsetDateTime.now()
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

        fun create(postId: String, now: OffsetDateTime = OffsetDateTime.now()): TrendingEntity {
            return TrendingEntity(
                postId = postId,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
