package ink.radiant.web.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import ink.radiant.query.service.TrendingPostService
import ink.radiant.web.codegen.types.Post

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

    companion object {
        private const val DEFAULT_LIMIT = 10
    }
}
