package ink.radiant.query.repository

import ink.radiant.query.entity.PostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface PostRepository : JpaRepository<PostEntity, String> {

    @Query("SELECT p FROM PostEntity p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(pageable: Pageable): Page<PostEntity>

    @Query("SELECT p FROM PostEntity p WHERE p.deletedAt IS NULL AND p.createdAt < :cursor ORDER BY p.createdAt DESC")
    fun findAllAfterCursor(cursor: OffsetDateTime, pageable: Pageable): Page<PostEntity>

    @Query("SELECT p FROM PostEntity p WHERE p.id = :id AND p.deletedAt IS NULL")
    fun findByIdAndNotDeleted(id: String): PostEntity?

    @Query("SELECT p FROM PostEntity p WHERE p.deletedAt IS NULL")
    fun findAllNotDeleted(pageable: Pageable): Page<PostEntity>
}
