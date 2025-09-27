package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.core.domain.model.PostAuthor
import ink.radiant.core.domain.model.PostParticipantConnection
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
import ink.radiant.web.codegen.types.UserEdge
import java.time.OffsetDateTime

@DgsComponent
class PostDataFetcher(
    private val postService: PostQueryService,
) {

    @DgsQuery
    fun posts(
        @InputArgument first: Int?,
        @InputArgument after: String?,
        dfe: DgsDataFetchingEnvironment,
    ): PostConnection {
        val connection = postService.findPosts(first, after)
        storePostsInContext(dfe, connection.edges.map { it.post })

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
    fun post(@InputArgument id: String, dfe: DgsDataFetchingEnvironment): PostResult {
        val post = postService.findPostById(id)
        post?.let { storePostsInContext(dfe, listOf(it)) }

        return post?.toGraphQLPost()
            ?: PostNotFoundError(
                message = "Post with id '$id' not found",
                code = "POST_NOT_FOUND",
                postId = id,
            )
    }

    @DgsData(parentType = POST_TYPE, field = PARTICIPANTS_FIELD)
    fun participants(
        dfe: DgsDataFetchingEnvironment,
        @InputArgument first: Int?,
        @InputArgument after: String?,
    ): UserConnection {
        val source = dfe.getSource<Post>()

        val context = (dfe.graphQlContext.get(CONTEXT_KEY) as? PostParticipantsContext)
            ?: PostParticipantsContext().also { dfe.graphQlContext.put(CONTEXT_KEY, it) }

        val domainPost = context.postsById[source.id]
        val postId = domainPost?.id?.toString() ?: source.id
        val effectiveFirst = first ?: DEFAULT_PARTICIPANT_LIMIT
        val connection = postService.findParticipants(postId, effectiveFirst, after)

        return connection.toGraphQLUserConnection()
    }

    @DgsData(parentType = POST_TYPE, field = AUTHOR_FIELD)
    fun author(dfe: DgsDataFetchingEnvironment): User? {
        val source = dfe.getSource<Post>()

        val context = (dfe.graphQlContext.get(CONTEXT_KEY) as? PostParticipantsContext)
            ?: PostParticipantsContext().also { dfe.graphQlContext.put(CONTEXT_KEY, it) }

        val domainPost = context.postsById[source.id]
        val authorId = domainPost?.authorId ?: source.author?.id ?: return null

        val cachedAuthor = context.authorsById[authorId]
        if (cachedAuthor != null) {
            return cachedAuthor.toGraphQLUser()
        }

        val author = postService.findAuthor(authorId) ?: return null
        context.authorsById[authorId] = author

        return author.toGraphQLUser()
    }

    private fun ink.radiant.core.domain.model.Post.toGraphQLPost(): Post = Post(
        id = this.id.toString(),
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
        author = null,
        participants = emptyUserConnection(),
    )

    private fun PostParticipantConnection.toGraphQLUserConnection(): UserConnection {
        return UserConnection(
            edges = this.edges.map { edge ->
                UserEdge(
                    node = edge.participant.toGraphQLUser(),
                    cursor = edge.cursor,
                )
            },
            pageInfo = PageInfo(
                hasNextPage = this.pageInfo.hasNextPage,
                hasPreviousPage = this.pageInfo.hasPreviousPage,
                startCursor = this.pageInfo.startCursor,
                endCursor = this.pageInfo.endCursor,
            ),
            totalCount = this.totalCount,
        )
    }

    private fun ink.radiant.core.domain.model.User.toGraphQLUser(): User = toGraphQLUser(
        id = this.id,
        username = this.username,
        name = this.name,
        avatarUrl = this.avatarUrl,
        bio = this.bio,
        location = this.location,
        websiteUrl = this.websiteUrl,
        joinedAt = this.joinedAt,
        postsCount = this.postsCount,
        viewsCount = this.viewsCount,
        followersCount = this.followersCount,
        followingCount = this.followingCount,
        professionalFields = this.professionalFields,
    )

    private fun toGraphQLUser(
        id: String,
        username: String,
        name: String,
        avatarUrl: String?,
        bio: String?,
        location: String?,
        websiteUrl: String?,
        joinedAt: OffsetDateTime,
        postsCount: Int,
        viewsCount: Long,
        followersCount: Int,
        followingCount: Int,
        professionalFields: Set<String>,
    ): User {
        val graphFields = professionalFields.mapNotNull { field ->
            runCatching { ProfessionalField.valueOf(field) }.getOrNull()
        }

        return User(
            id = id,
            username = username,
            name = name,
            avatarUrl = avatarUrl,
            bio = bio,
            location = location,
            websiteUrl = websiteUrl,
            joinedAt = joinedAt,
            postsCount = postsCount,
            viewsCount = viewsCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            followersCount = followersCount,
            followingCount = followingCount,
            professionalFields = graphFields,
            followers = emptyUserConnection(),
            following = emptyUserConnection(),
            posts = emptyPostConnection(),
        )
    }

    private fun emptyUserConnection(): UserConnection = UserConnection(
        edges = emptyList(),
        pageInfo = PageInfo(
            hasNextPage = false,
            hasPreviousPage = false,
            startCursor = null,
            endCursor = null,
        ),
        totalCount = 0,
    )

    private fun emptyPostConnection(): PostConnection = PostConnection(
        edges = emptyList(),
        pageInfo = PageInfo(
            hasNextPage = false,
            hasPreviousPage = false,
            startCursor = null,
            endCursor = null,
        ),
        totalCount = 0,
    )

    private fun storePostsInContext(dfe: DgsDataFetchingEnvironment, posts: List<ink.radiant.core.domain.model.Post>) {
        if (posts.isEmpty()) {
            return
        }

        val context = (dfe.graphQlContext.get(CONTEXT_KEY) as? PostParticipantsContext)
            ?: PostParticipantsContext().also { dfe.graphQlContext.put(CONTEXT_KEY, it) }

        posts.forEach { post ->
            context.postsById[post.id.toString()] = post
        }
    }

    companion object {
        private const val DEFAULT_PARTICIPANT_LIMIT = 10
        private const val CONTEXT_KEY = "radiant-post-context"
        private const val POST_TYPE = "Post"
        private const val PARTICIPANTS_FIELD = "participants"
        private const val AUTHOR_FIELD = "author"
    }
}

private class PostParticipantsContext(
    val postsById: MutableMap<String, ink.radiant.core.domain.model.Post> = mutableMapOf(),
    val authorsById: MutableMap<String, PostAuthor> = mutableMapOf(),
)
