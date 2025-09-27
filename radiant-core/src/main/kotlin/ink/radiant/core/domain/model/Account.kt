package ink.radiant.core.domain.model

data class Account(
    val id: String,
    val username: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val provider: String,
)

typealias OAuthAccount = Account
