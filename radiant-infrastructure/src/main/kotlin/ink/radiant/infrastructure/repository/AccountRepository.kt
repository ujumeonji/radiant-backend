package ink.radiant.infrastructure.repository

import ink.radiant.infrastructure.entity.AccountEntity
import ink.radiant.infrastructure.entity.OAuthProvider
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : CrudRepository<AccountEntity, String> {

    fun findByProviderIdAndProvider(providerId: String, provider: OAuthProvider): AccountEntity?

    fun existsByName(name: String): Boolean
}
