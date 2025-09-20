package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.query.service.TrendingPostService
import ink.radiant.web.codegen.types.Post
import ink.radiant.web.codegen.types.Topic

@DgsComponent
class TrendingDataFetcher(
    private val trendingPostService: TrendingPostService,
) {

    @DgsQuery
    fun trendingPosts(@InputArgument limit: Int?): List<Post> {
        val requestedLimit = limit ?: DEFAULT_LIMIT
        val trendingPosts = trendingPostService.findTrendingPosts(requestedLimit)

        return trendingPosts.map { post ->
            post.toGraphQLPost()
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

    @DgsQuery
    fun popularTopics(@InputArgument limit: Int?): List<Topic> {
        val requestedLimit = limit ?: DEFAULT_TOPICS_LIMIT
        return generateMockTopics().take(requestedLimit)
    }

    private fun generateMockTopics(): List<Topic> {
        return listOf(
            Topic(
                id = "topic_1",
                name = "Kotlin",
                slug = "kotlin",
                postCount = 156,
            ),
            Topic(
                id = "topic_2",
                name = "Spring Boot",
                slug = "spring-boot",
                postCount = 143,
            ),
            Topic(
                id = "topic_3",
                name = "Machine Learning",
                slug = "machine-learning",
                postCount = 128,
            ),
            Topic(
                id = "topic_4",
                name = "React",
                slug = "react",
                postCount = 119,
            ),
            Topic(
                id = "topic_5",
                name = "GraphQL",
                slug = "graphql",
                postCount = 98,
            ),
            Topic(
                id = "topic_6",
                name = "Docker",
                slug = "docker",
                postCount = 87,
            ),
            Topic(
                id = "topic_7",
                name = "Microservices",
                slug = "microservices",
                postCount = 76,
            ),
            Topic(
                id = "topic_8",
                name = "Vue.js",
                slug = "vuejs",
                postCount = 65,
            ),
            Topic(
                id = "topic_9",
                name = "TypeScript",
                slug = "typescript",
                postCount = 54,
            ),
            Topic(
                id = "topic_10",
                name = "DevOps",
                slug = "devops",
                postCount = 43,
            ),
        )
    }

    companion object {
        private const val DEFAULT_LIMIT = 10
        private const val DEFAULT_TOPICS_LIMIT = 5
    }
}
