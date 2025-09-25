package ink.radiant.core.domain.model

import java.time.OffsetDateTime

/**
 * Represents a participant involved with a post alongside pagination helpers.
 */
data class PostParticipantConnection(
    val edges: List<PostParticipantEdge>,
    val pageInfo: PageInfo,
    val totalCount: Int,
)

data class PostParticipantEdge(
    val participant: PostParticipant,
    val cursor: String,
)

data class PostParticipant(
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
