package ink.radiant.core.domain.model

data class PostConnection(
    val edges: List<PostEdge>,
    val pageInfo: PageInfo,
)

data class PostEdge(
    val post: Post,
    val cursor: String,
)

data class PageInfo(
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val startCursor: String?,
    val endCursor: String?,
)
