package ink.radiant.infrastructure.repository

import ink.radiant.infrastructure.entity.ProfileEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository : CrudRepository<ProfileEntity, String> {

    fun existsByDisplayName(displayName: String): Boolean
}
