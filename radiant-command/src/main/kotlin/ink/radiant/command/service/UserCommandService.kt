package ink.radiant.command.service

import ink.radiant.core.domain.model.Account
import ink.radiant.core.domain.model.OAuthAccount
import ink.radiant.query.service.UserQueryService

interface UserCommandService : UserQueryService {
    fun findOrCreateUser(oauthAccount: OAuthAccount): Account
}
