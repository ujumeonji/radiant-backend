package ink.radiant.query.service

import ink.radiant.core.domain.model.Post
import ink.radiant.core.domain.model.PostConnection
interface PostQueryService {
    fun findPosts(first: Int?, after: String?): PostConnection
    fun findPostById(id: String): Post?
    fun findPostsByIds(ids: List<String>): List<Post>
}
