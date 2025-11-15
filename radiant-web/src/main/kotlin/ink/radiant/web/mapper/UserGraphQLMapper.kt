package ink.radiant.web.mapper

import ink.radiant.web.codegen.types.PageInfo
import ink.radiant.web.codegen.types.PostConnection
import ink.radiant.web.codegen.types.ProfessionalField
import ink.radiant.web.codegen.types.UserConnection
import ink.radiant.core.domain.model.User as DomainUser
import ink.radiant.web.codegen.types.User as GraphQLUser

object UserGraphQLMapper {

    fun toGraphQLUser(domainUser: DomainUser): GraphQLUser {
        return GraphQLUser(
            id = domainUser.id,
            username = domainUser.username,
            name = domainUser.name,
            avatarUrl = domainUser.avatarUrl,
            bio = domainUser.bio,
            location = domainUser.location,
            websiteUrl = domainUser.websiteUrl,
            joinedAt = domainUser.joinedAt,
            postsCount = domainUser.postsCount,
            viewsCount = domainUser.viewsCount.toInt(),
            followersCount = domainUser.followersCount,
            followingCount = domainUser.followingCount,
            professionalFields = domainUser.professionalFields.mapNotNull { field ->
                try {
                    ProfessionalField.valueOf(field)
                } catch (e: IllegalArgumentException) {
                    null
                }
            },
            followers = UserConnection(
                edges = emptyList(),
                pageInfo = PageInfo(
                    hasNextPage = false,
                    hasPreviousPage = false,
                    startCursor = null,
                    endCursor = null,
                ),
                totalCount = domainUser.followersCount,
            ),
            following = UserConnection(
                edges = emptyList(),
                pageInfo = PageInfo(
                    hasNextPage = false,
                    hasPreviousPage = false,
                    startCursor = null,
                    endCursor = null,
                ),
                totalCount = domainUser.followingCount,
            ),
            posts = PostConnection(
                edges = emptyList(),
                pageInfo = PageInfo(
                    hasNextPage = false,
                    hasPreviousPage = false,
                    startCursor = null,
                    endCursor = null,
                ),
                totalCount = domainUser.postsCount,
            ),
        )
    }
}
