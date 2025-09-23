package ink.radiant.command.repository

import ink.radiant.command.entity.PostEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PostRepository : JpaRepository<PostEntity, String> {

    @Query("SELECT p FROM PostEntity p WHERE p.id = :id AND p.deletedAt IS NULL")
    override fun findById(id: String): Optional<PostEntity>

    @Query("SELECT p FROM PostEntity p WHERE p.deletedAt IS NULL")
    override fun findAll(): List<PostEntity>
}
