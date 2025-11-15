package ink.radiant.command.service

import ink.radiant.core.domain.entity.AccountEntity
import ink.radiant.core.domain.entity.OAuthProvider
import ink.radiant.core.domain.model.Account
import ink.radiant.core.domain.model.OAuthAccount
import ink.radiant.core.domain.model.User
import ink.radiant.infrastructure.mapper.UserMapper
import ink.radiant.infrastructure.repository.AccountRepository
import ink.radiant.infrastructure.repository.ProfileRepository
import ink.radiant.query.service.UserQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val accountRepository: AccountRepository,
    private val profileRepository: ProfileRepository,
) : UserCommandService, UserQueryService {

    @Transactional(readOnly = true)
    override fun findUserByAccountId(accountId: String): User? {
        val account = accountRepository.findById(accountId).orElse(null) ?: return null
        val profile = profileRepository.findByAccountId(accountId) ?: return null

        return UserMapper.toUser(account, profile)
    }

    @Transactional(readOnly = true)
    override fun findUserByUsername(username: String): User? {
        val profile = profileRepository.findByDisplayName(username) ?: return null
        val account = accountRepository.findById(profile.account.id).orElse(null) ?: return null

        return UserMapper.toUser(account, profile)
    }

    @Transactional
    override fun findOrCreateUser(oauthAccount: OAuthAccount): Account {
        val provider = OAuthProvider.valueOf(oauthAccount.provider.uppercase())
        val existingAccount =
            accountRepository.findByProviderIdAndProvider(oauthAccount.id, provider)

        return if (existingAccount != null) {
            updateLastLoginInfo(existingAccount)
        } else {
            createNewUser(oauthAccount)
        }
    }

    private fun updateLastLoginInfo(existingAccount: AccountEntity): Account {
        existingAccount.updateLastLogin()

        val savedAccount = accountRepository.save(existingAccount)
        return convertToUser(savedAccount)
    }

    private fun createNewUser(oauthAccount: OAuthAccount): Account {
        val provider = OAuthProvider.valueOf(oauthAccount.provider.uppercase())
        val displayName = generateUniqueDisplayName(oauthAccount.username)

        val account = AccountEntity.signUp(
            email = oauthAccount.email,
            name = oauthAccount.name,
            provider = provider,
            providerId = oauthAccount.id,
            displayName = displayName,
            avatarUrl = oauthAccount.avatarUrl,
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
