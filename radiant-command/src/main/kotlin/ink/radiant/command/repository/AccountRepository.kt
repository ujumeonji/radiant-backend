package ink.radiant.command.repository

import ink.radiant.command.entity.AccountEntity
import ink.radiant.command.entity.OAuthProvider
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
@Repository
interface AccountRepository : CrudRepository<AccountEntity, String> {

    fun findByProviderIdAndProvider(providerId: String, provider: OAuthProvider): AccountEntity?

    fun existsByName(name: String): Boolean
}
