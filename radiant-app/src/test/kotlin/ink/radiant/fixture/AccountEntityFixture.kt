package ink.radiant.fixture

import ink.radiant.core.domain.entity.AccountEntity
import ink.radiant.core.domain.entity.OAuthProvider

object AccountEntityFixture {

    fun createAccount(
        email: String,
        name: String,
        provider: OAuthProvider = OAuthProvider.GITHUB,
        providerId: String,
        displayName: String,
        avatarUrl: String? = null,
    ): AccountEntity {
        return AccountEntity.signUp(
            email = email,
            name = name,
            provider = provider,
            providerId = providerId,
            displayName = displayName,
            avatarUrl = avatarUrl,
        )
    }
}
