package ink.radiant.command.service

import ink.radiant.command.entity.AccountEntity
import ink.radiant.command.entity.OAuthProvider
import ink.radiant.command.repository.AccountRepository
import ink.radiant.command.repository.ProfileRepository
import ink.radiant.core.domain.model.OAuthUser
import ink.radiant.core.domain.model.User
import ink.radiant.query.service.UserQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserServiceImpl(
    private val accountRepository: AccountRepository,
    private val profileRepository: ProfileRepository,
) : UserCommandService, UserQueryService {

    override fun findOrCreateUser(oauthUser: OAuthUser): User {
        val provider = OAuthProvider.valueOf(oauthUser.provider.uppercase())
        val existingAccount =
            accountRepository.findByProviderIdAndProvider(oauthUser.id, provider)

        return if (existingAccount != null) {
            updateLastLoginInfo(existingAccount)
        } else {
            createNewUser(oauthUser)
        }
    }

    private fun updateLastLoginInfo(existingAccount: AccountEntity): User {
        existingAccount.updateLastLogin()

        val savedAccount = accountRepository.save(existingAccount)
        return convertToUser(savedAccount)
    }

    private fun createNewUser(oauthUser: OAuthUser): User {
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

    private fun convertToUser(account: AccountEntity): User = User(
        id = account.id,
        username = account.name,
        name = account.displayName,
        email = account.email,
        avatarUrl = account.avatarUrl,
        provider = account.providerName,
    )
}
