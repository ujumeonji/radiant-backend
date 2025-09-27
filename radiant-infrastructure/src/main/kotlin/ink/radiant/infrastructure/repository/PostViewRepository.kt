package ink.radiant.infrastructure.repository

import ink.radiant.infrastructure.view.PostViewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PostViewRepository : JpaRepository<PostViewEntity, String> {

    @Query("SELECT p FROM PostViewEntity p WHERE p.id = :id AND p.deletedAt IS NULL")
    override fun findById(id: String): Optional<PostViewEntity>

    @Query("SELECT p FROM PostViewEntity p WHERE p.deletedAt IS NULL")
    override fun findAll(): List<PostViewEntity>

    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
}
