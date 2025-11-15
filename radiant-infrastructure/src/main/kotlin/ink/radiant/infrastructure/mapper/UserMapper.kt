package ink.radiant.infrastructure.mapper

import ink.radiant.core.domain.entity.AccountEntity
import ink.radiant.core.domain.entity.ProfileEntity
import ink.radiant.core.domain.model.User

object UserMapper {

    fun toUser(account: AccountEntity, profile: ProfileEntity): User {
        return User(
            id = account.id,
            username = profile.displayName,
            name = account.name,
            avatarUrl = profile.avatarUrl,
            bio = profile.bio,
            location = profile.location,
            websiteUrl = profile.websiteUrl,
            joinedAt = account.createdAt!!,
            postsCount = profile.postsCount,
            viewsCount = profile.viewsCount.toLong(),
            followersCount = profile.followersCount,
            followingCount = profile.followingCount,
            professionalFields = profile.professionalFields.map { it.name }.toSet(),
        )
    }
}
