package ink.radiant.command.repository

import ink.radiant.command.entity.ProfileEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProfileRepository : CrudRepository<ProfileEntity, UUID> {

    fun existsByDisplayName(displayName: String): Boolean
}
