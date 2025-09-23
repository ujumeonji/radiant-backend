package ink.radiant.command.service

import ink.radiant.core.domain.model.User

interface UserCommandService {
    fun findOrCreateUser(oauthUser: User): User
}
