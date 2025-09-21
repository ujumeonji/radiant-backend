package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.query.service.PostQueryService
import ink.radiant.web.codegen.types.PageInfo
import ink.radiant.web.codegen.types.Post
import ink.radiant.web.codegen.types.PostConnection
import ink.radiant.web.codegen.types.PostEdge
import ink.radiant.web.codegen.types.PostNotFoundError
import ink.radiant.web.codegen.types.PostResult
import ink.radiant.web.codegen.types.ProfessionalField
import ink.radiant.web.codegen.types.User
import ink.radiant.web.codegen.types.UserConnection
import java.time.OffsetDateTime

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
            totalCount = connection.totalCount,
        )
    }

    @DgsQuery
    fun post(@InputArgument id: String): PostResult {
        val post = postQueryService.findPostById(id)
        return if (post != null) {
            post.toGraphQLPost()
        } else {
            PostNotFoundError(
                message = "Post with id '$id' not found",
                code = "POST_NOT_FOUND",
                postId = id,
            )
        }
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
        likesCount = this.likes,
        commentsCount = this.commentsCount,
        thumbnailUrl = this.thumbnailUrl,
        author = this.authorId?.let { createMockAuthor(it) },
        participants = UserConnection(
            edges = emptyList(),
            pageInfo = PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = null,
                endCursor = null,
            ),
            totalCount = 0,
        ),
    )
}

private fun createMockAuthor(authorId: String): User {
    return User(
        id = authorId,
        username = "user_$authorId",
        name = "Mock User $authorId",
        avatarUrl = "https://example.com/avatar_$authorId.jpg",
        bio = "Mock author for post",
        location = "Seoul, South Korea",
        websiteUrl = null,
        joinedAt = OffsetDateTime.now().minusMonths(6),
        postsCount = 1,
        viewsCount = 100,
        followersCount = 10,
        followingCount = 5,
        professionalFields = listOf(ProfessionalField.BACKEND),
        followers = UserConnection(
            edges = emptyList(),
            pageInfo = PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = null,
                endCursor = null,
            ),
            totalCount = 0,
        ),
        following = UserConnection(
            edges = emptyList(),
            pageInfo = PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = null,
                endCursor = null,
            ),
            totalCount = 0,
        ),
        posts = PostConnection(
            edges = emptyList(),
            pageInfo = PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = null,
                endCursor = null,
            ),
            totalCount = 0,
        ),
    )
}
