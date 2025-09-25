package ink.radiant.core.domain.model

import java.time.OffsetDateTime

data class PostAuthor(
    val id: String,
    val username: String,
    val name: String,
    val avatarUrl: String?,
    val bio: String?,
    val location: String?,
    val websiteUrl: String?,
    val joinedAt: OffsetDateTime,
    val postsCount: Int,
    val viewsCount: Long,
    val followersCount: Int,
    val followingCount: Int,
    val professionalFields: Set<String>,
)
