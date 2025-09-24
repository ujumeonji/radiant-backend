package ink.radiant.query.service

import ink.radiant.core.domain.model.Post

interface TrendingQueryService {
    fun findTrendingPosts(limit: Int = 10): List<Post>
}
