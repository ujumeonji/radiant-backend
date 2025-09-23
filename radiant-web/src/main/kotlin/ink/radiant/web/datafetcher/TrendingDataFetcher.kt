package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.query.service.TrendingQueryService
import ink.radiant.web.codegen.types.PageInfo
import ink.radiant.web.codegen.types.Post
import ink.radiant.web.codegen.types.PostConnection
import ink.radiant.web.codegen.types.PostEdge
import ink.radiant.web.codegen.types.Topic
import ink.radiant.web.codegen.types.TopicConnection
import ink.radiant.web.codegen.types.TopicEdge

@DgsComponent
class TrendingDataFetcher(
    private val trendingQueryService: TrendingQueryService,
) {

    @DgsQuery
    fun trendingPosts(@InputArgument first: Int?, @InputArgument after: String?): PostConnection {
        val requestedLimit = first ?: DEFAULT_LIMIT
        val trendingPosts = trendingQueryService.findTrendingPosts(requestedLimit)

        return PostConnection(
            edges = trendingPosts.mapIndexed { index, post ->
                PostEdge(
                    node = post.toGraphQLPost(),
                    cursor = "cursor_${index + 1}",
                )
            },
            pageInfo = PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = if (trendingPosts.isNotEmpty()) "cursor_1" else null,
                endCursor = if (trendingPosts.isNotEmpty()) "cursor_${trendingPosts.size}" else null,
            ),
            totalCount = trendingPosts.size,
        )
    }

    private fun ink.radiant.core.domain.model.Post.toGraphQLPost(): Post {
        return Post(
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
            participants = ink.radiant.web.codegen.types.UserConnection(
                edges = emptyList(),
                pageInfo = ink.radiant.web.codegen.types.PageInfo(
                    hasNextPage = false,
                    hasPreviousPage = false,
                    startCursor = null,
                    endCursor = null,
                ),
                totalCount = 0,
            ),
        )
    }

    @DgsQuery
    fun popularTopics(@InputArgument first: Int?, @InputArgument after: String?): TopicConnection {
        val requestedLimit = first ?: DEFAULT_TOPICS_LIMIT
        val topics = generateMockTopics().take(requestedLimit)

        return TopicConnection(
            edges = topics.mapIndexed { index, topic ->
                TopicEdge(
                    node = topic,
                    cursor = "topic_cursor_${index + 1}",
                )
            },
            pageInfo = PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = if (topics.isNotEmpty()) "topic_cursor_1" else null,
                endCursor = if (topics.isNotEmpty()) "topic_cursor_${topics.size}" else null,
            ),
            totalCount = topics.size,
        )
    }

    private fun generateMockTopics(): List<Topic> {
        return listOf(
            Topic(
                id = "topic_1",
                name = "Kotlin",
                slug = "kotlin",
                postsCount = 156,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_2",
                name = "Spring Boot",
                slug = "spring-boot",
                postsCount = 143,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_3",
                name = "Machine Learning",
                slug = "machine-learning",
                postsCount = 128,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_4",
                name = "React",
                slug = "react",
                postsCount = 119,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_5",
                name = "GraphQL",
                slug = "graphql",
                postsCount = 98,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_6",
                name = "Docker",
                slug = "docker",
                postsCount = 87,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_7",
                name = "Microservices",
                slug = "microservices",
                postsCount = 76,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_8",
                name = "Vue.js",
                slug = "vuejs",
                postsCount = 65,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_9",
                name = "TypeScript",
                slug = "typescript",
                postsCount = 54,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
                        hasNextPage = false,
                        hasPreviousPage = false,
                        startCursor = null,
                        endCursor = null,
                    ),
                    totalCount = 0,
                ),
            ),
            Topic(
                id = "topic_10",
                name = "DevOps",
                slug = "devops",
                postsCount = 43,
                posts = PostConnection(
                    edges = emptyList(),
                    pageInfo = ink.radiant.web.codegen.types.PageInfo(
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

    companion object {
        private const val DEFAULT_LIMIT = 10
        private const val DEFAULT_TOPICS_LIMIT = 5
    }
}
