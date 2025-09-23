package ink.radiant.command.repository

import ink.radiant.command.entity.ProfileEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
@Repository
interface ProfileRepository : CrudRepository<ProfileEntity, String> {

    fun existsByDisplayName(displayName: String): Boolean
}
