package ink.radiant.core.domain.model

data class PostConnection(
    val edges: List<PostEdge>,
    val pageInfo: PageInfo,
    val totalCount: Int = 0,
)

data class PostEdge(
    val post: Post,
    val cursor: String,
)
