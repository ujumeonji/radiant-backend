package ink.radiant.infrastructure.repository

import ink.radiant.infrastructure.entity.PostParticipantEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface PostParticipantRepository : JpaRepository<PostParticipantEntity, String> {

    @Query(
        "SELECT pp FROM PostParticipantEntity pp " +
            "WHERE pp.post.id = :postId AND pp.deletedAt IS NULL " +
            "AND (:cursorCreatedAt IS NULL OR pp.createdAt > :cursorCreatedAt " +
            "   OR (pp.createdAt = :cursorCreatedAt AND (:cursorId IS NULL OR pp.id > :cursorId))) " +
            "ORDER BY pp.createdAt ASC, pp.id ASC",
    )
    fun findActiveByPostId(
        postId: String,
        cursorCreatedAt: OffsetDateTime?,
        cursorId: String?,
        pageable: Pageable,
    ): List<PostParticipantEntity>

    @Query("SELECT COUNT(pp) FROM PostParticipantEntity pp WHERE pp.post.id = :postId AND pp.deletedAt IS NULL")
    fun countActiveByPostId(postId: String): Long
}
