package ink.radiant.command.service

import ink.radiant.core.domain.model.Account

interface UserCommandService {
    fun findOrCreateUser(oauthAccount: Account): Account
}
