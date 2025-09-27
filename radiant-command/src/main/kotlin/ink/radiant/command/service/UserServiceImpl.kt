package ink.radiant.command.service

import ink.radiant.core.domain.entity.AccountEntity
import ink.radiant.core.domain.entity.OAuthProvider
import ink.radiant.core.domain.model.Account
import ink.radiant.core.domain.model.OAuthAccount
import ink.radiant.infrastructure.repository.AccountRepository
import ink.radiant.infrastructure.repository.ProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserServiceImpl(
    private val accountRepository: AccountRepository,
    private val profileRepository: ProfileRepository,
) : UserCommandService {

    override fun findOrCreateUser(oauthUser: OAuthAccount): Account {
        val provider = OAuthProvider.valueOf(oauthUser.provider.uppercase())
        val existingAccount =
            accountRepository.findByProviderIdAndProvider(oauthUser.id, provider)

        return if (existingAccount != null) {
            updateLastLoginInfo(existingAccount)
        } else {
            createNewUser(oauthUser)
        }
    }

    private fun updateLastLoginInfo(existingAccount: AccountEntity): Account {
        existingAccount.updateLastLogin()

        val savedAccount = accountRepository.save(existingAccount)
        return convertToUser(savedAccount)
    }

    private fun createNewUser(oauthUser: OAuthAccount): Account {
        val provider = OAuthProvider.valueOf(oauthUser.provider.uppercase())
        val displayName = generateUniqueDisplayName(oauthUser.username)

        val account = AccountEntity.signUp(
            email = oauthUser.email,
            name = oauthUser.name,
            provider = provider,
            providerId = oauthUser.id,
            displayName = displayName,
            avatarUrl = oauthUser.avatarUrl,
        )

        val savedAccount = accountRepository.save(account)

        return convertToUser(savedAccount)
    }

    private fun generateUniqueDisplayName(baseUsername: String): String {
        var username = baseUsername
        var counter = 1

        while (profileRepository.existsByDisplayName(username)) {
            username = "${baseUsername}_$counter"
            counter++
        }

        return username
    }

    private fun convertToUser(account: AccountEntity): Account = Account(
        id = account.id,
        username = account.name,
        name = account.displayName,
        email = account.email,
        avatarUrl = account.avatarUrl,
        provider = account.providerName,
    )
}
