package ink.radiant.core.domain.model

data class PostParticipantConnection(
    val edges: List<PostParticipantEdge>,
    val pageInfo: PageInfo,
    val totalCount: Int,
)

data class PostParticipantEdge(
    val participant: PostParticipant,
    val cursor: String,
)

typealias PostParticipant = User
