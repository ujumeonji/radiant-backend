package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.query.service.PostQueryService
import ink.radiant.web.codegen.types.PageInfo
import ink.radiant.web.codegen.types.Post
import ink.radiant.web.codegen.types.PostConnection
import ink.radiant.web.codegen.types.PostEdge

@DgsComponent
class PostDataFetcher(
    private val postQueryService: PostQueryService,
) {

    @DgsQuery
    fun posts(@InputArgument first: Int?, @InputArgument after: String?): PostConnection {
        val connection = postQueryService.findPosts(first, after)

        return PostConnection(
            edges = connection.edges.map { edge ->
                PostEdge(
                    node = edge.post.toGraphQLPost(),
                    cursor = edge.cursor,
                )
            },
            pageInfo = PageInfo(
                hasNextPage = connection.pageInfo.hasNextPage,
                hasPreviousPage = connection.pageInfo.hasPreviousPage,
                startCursor = connection.pageInfo.startCursor,
                endCursor = connection.pageInfo.endCursor,
            ),
        )
    }

    @DgsQuery
    fun post(@InputArgument id: String): Post? {
        return postQueryService.findPostById(id)?.toGraphQLPost()
    }
}

private fun ink.radiant.core.domain.model.Post.toGraphQLPost(): Post {
    return Post(
        id = this.id,
        title = this.title,
        body = this.body,
        translatedTitle = this.translatedTitle,
        originalSentences = this.originalSentences,
        translatedSentences = this.translatedSentences,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        likes = this.likes,
        commentsCount = this.commentsCount,
        thumbnailUrl = this.thumbnailUrl,
        author = null,
        participants = emptyList(),
    )
}
