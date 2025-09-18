package ink.radiant.query.repository

import ink.radiant.query.entity.TrendingEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface TrendingRepository : JpaRepository<TrendingEntity, String> {

    @Query(
        """
        SELECT t FROM TrendingEntity t
        WHERE t.lastViewedAt >= :since AND t.deletedAt IS NULL
        ORDER BY t.trendScore DESC, t.lastViewedAt DESC
    """,
    )
    fun findTrendingPostsSince(since: OffsetDateTime, pageable: Pageable): List<TrendingEntity>

    @Query(
        """
        SELECT t FROM TrendingEntity t
        WHERE t.deletedAt IS NULL
        ORDER BY t.trendScore DESC, t.lastViewedAt DESC
    """,
    )
    fun findAllTrendingPosts(pageable: Pageable): List<TrendingEntity>

    @Query("SELECT t FROM TrendingEntity t WHERE t.postId = :postId AND t.deletedAt IS NULL")
    fun findByPostIdAndNotDeleted(postId: String): TrendingEntity?
}
