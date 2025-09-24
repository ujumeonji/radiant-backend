package ink.radiant.infrastructure.repository

import ink.radiant.infrastructure.entity.TrendingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TrendingCommandRepository : JpaRepository<TrendingEntity, String> {

    @Query("SELECT t FROM TrendingEntity t WHERE t.postId = :postId AND t.deletedAt IS NULL")
    fun findByPostId(postId: String): TrendingEntity?
}
