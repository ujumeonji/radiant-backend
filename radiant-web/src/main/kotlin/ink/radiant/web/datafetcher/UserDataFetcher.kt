package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.web.codegen.types.PageInfo
import ink.radiant.web.codegen.types.Post
import ink.radiant.web.codegen.types.PostConnection
import ink.radiant.web.codegen.types.PostEdge
import ink.radiant.web.codegen.types.ProfessionalField
import ink.radiant.web.codegen.types.User
import ink.radiant.web.codegen.types.UserConnection
import ink.radiant.web.codegen.types.UserEdge
import java.time.OffsetDateTime

@DgsComponent
class UserDataFetcher {

    @DgsQuery
    fun recommendedAuthors(@InputArgument first: Int?, @InputArgument after: String?): UserConnection {
        val requestedLimit = first ?: DEFAULT_RECOMMENDED_LIMIT
        val users = generateMockUsers().take(requestedLimit)

        return UserConnection(
            edges = users.mapIndexed { index, user ->
                UserEdge(
                    node = user,
                    cursor = "user_cursor_${index + 1}",
                )
            },
            pageInfo = PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = if (users.isNotEmpty()) "user_cursor_1" else null,
                endCursor = if (users.isNotEmpty()) "user_cursor_${users.size}" else null,
            ),
            totalCount = users.size,
        )
    }

    @DgsQuery
    fun user(@InputArgument username: String): User? {
        return generateMockUsers().find { it.username == username }
    }

    @DgsQuery
    fun userPosts(
        @InputArgument username: String,
        @InputArgument first: Int?,
        @InputArgument after: String?,
    ): PostConnection {
        val user = generateMockUsers().find { it.username == username }
            ?: return PostConnection(
                edges = emptyList(),
                pageInfo = PageInfo(
                    hasNextPage = false,
                    hasPreviousPage = false,
                    startCursor = null,
                    endCursor = null,
                ),
                totalCount = 0,
            )

        val mockPosts = generateMockPostsForUser(user)
        val requestedLimit = first ?: DEFAULT_POSTS_LIMIT
        val posts = mockPosts.take(requestedLimit)

        return PostConnection(
            edges = posts.mapIndexed { index, post ->
                PostEdge(
                    node = post,
                    cursor = "cursor_${index + 1}",
                )
            },
            pageInfo = PageInfo(
                hasNextPage = posts.size == requestedLimit && mockPosts.size > requestedLimit,
                hasPreviousPage = false,
                startCursor = if (posts.isNotEmpty()) "cursor_1" else null,
                endCursor = if (posts.isNotEmpty()) "cursor_${posts.size}" else null,
            ),
            totalCount = mockPosts.size,
        )
    }

    private fun generateMockUsers(): List<User> {
        return listOf(
            User(
                id = "user_1",
                username = "john_doe",
                name = "John Doe",
                avatarUrl = "https://example.com/avatar1.jpg",
                bio = "Backend developer passionate about Kotlin and Spring Boot",
                location = "Seoul, South Korea",
                websiteUrl = "https://johndoe.dev",
                joinedAt = OffsetDateTime.now().minusMonths(6),
                postsCount = 25,
                viewsCount = 1500,
                followersCount = 150,
                followingCount = 80,
                professionalFields = listOf(ProfessionalField.BACKEND),
                followers = ink.radiant.web.codegen.types.UserConnection(
                    edges = emptyList(),
                    pageInfo = PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
                following = ink.radiant.web.codegen.types.UserConnection(
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
            ),
            User(
                id = "user_2",
                username = "jane_smith",
                name = "Jane Smith",
                avatarUrl = "https://example.com/avatar2.jpg",
                bio = "Frontend engineer and UI/UX enthusiast",
                location = "Busan, South Korea",
                websiteUrl = "https://janesmith.com",
                joinedAt = OffsetDateTime.now().minusMonths(8),
                postsCount = 42,
                viewsCount = 2800,
                followersCount = 320,
                followingCount = 120,
                professionalFields = listOf(ProfessionalField.FRONTEND),
                followers = ink.radiant.web.codegen.types.UserConnection(
                    edges = emptyList(),
                    pageInfo = PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
                following = ink.radiant.web.codegen.types.UserConnection(
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
            ),
            User(
                id = "user_3",
                username = "ai_researcher",
                name = "Dr. Sarah Kim",
                avatarUrl = "https://example.com/avatar3.jpg",
                bio = "AI/ML researcher and data scientist",
                location = "Daejeon, South Korea",
                websiteUrl = "https://sarahkim.ai",
                joinedAt = OffsetDateTime.now().minusMonths(12),
                postsCount = 38,
                viewsCount = 5200,
                followersCount = 680,
                followingCount = 95,
                professionalFields = listOf(ProfessionalField.AI_ML),
                followers = ink.radiant.web.codegen.types.UserConnection(
                    edges = emptyList(),
                    pageInfo = PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
                following = ink.radiant.web.codegen.types.UserConnection(
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
            ),
            User(
                id = "user_4",
                username = "fullstack_dev",
                name = "Alex Park",
                avatarUrl = "https://example.com/avatar4.jpg",
                bio = "Full-stack developer working with modern web technologies",
                location = "Incheon, South Korea",
                websiteUrl = null,
                joinedAt = OffsetDateTime.now().minusMonths(3),
                postsCount = 18,
                viewsCount = 900,
                followersCount = 75,
                followingCount = 45,
                professionalFields = listOf(ProfessionalField.BACKEND, ProfessionalField.FRONTEND),
                followers = ink.radiant.web.codegen.types.UserConnection(
                    edges = emptyList(),
                    pageInfo = PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
                following = ink.radiant.web.codegen.types.UserConnection(
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
            ),
            User(
                id = "user_5",
                username = "ml_engineer",
                name = "Chris Lee",
                avatarUrl = "https://example.com/avatar5.jpg",
                bio = "Machine learning engineer focused on deep learning applications",
                location = "Gwangju, South Korea",
                websiteUrl = "https://chrislee.ml",
                joinedAt = OffsetDateTime.now().minusMonths(10),
                postsCount = 31,
                viewsCount = 3400,
                followersCount = 420,
                followingCount = 110,
                professionalFields = listOf(ProfessionalField.AI_ML),
                followers = ink.radiant.web.codegen.types.UserConnection(
                    edges = emptyList(),
                    pageInfo = PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
                following = ink.radiant.web.codegen.types.UserConnection(
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
            ),
        )
    }

    private fun generateMockPostsForUser(user: User): List<Post> {
        val baseTime = OffsetDateTime.now()
        return (1..user.postsCount).map { index ->
            Post(
                id = "post_${user.id}_$index",
                title = "Sample Post $index by ${user.name}",
                body = "This is a mock post content written by ${user.name}. " +
                    "This post demonstrates the GraphQL API functionality.",
                translatedTitle = "샘플 포스트 $index by ${user.name}",
                originalSentences = listOf(
                    "This is a mock post content written by ${user.name}.",
                    "This post demonstrates the GraphQL API functionality.",
                ),
                translatedSentences = listOf(
                    "이것은 ${user.name}이 작성한 모의 포스트 내용입니다.",
                    "이 포스트는 GraphQL API 기능을 보여줍니다.",
                ),
                createdAt = baseTime.minusDays(index.toLong()),
                updatedAt = baseTime.minusDays(index.toLong()),
                likesCount = (10..100).random(),
                commentsCount = (0..20).random(),
                thumbnailUrl = "https://example.com/thumbnail_${user.id}_$index.jpg",
                author = user,
                participants = ink.radiant.web.codegen.types.UserConnection(
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
    }

    companion object {
        private const val DEFAULT_RECOMMENDED_LIMIT = 5
        private const val DEFAULT_POSTS_LIMIT = 10
    }
}
