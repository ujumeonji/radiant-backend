package ink.radiant.query.service

import ink.radiant.core.domain.model.User

interface UserQueryService {
    fun findUserByAccountId(accountId: String): User?
    fun findUserByUsername(username: String): User?
}
