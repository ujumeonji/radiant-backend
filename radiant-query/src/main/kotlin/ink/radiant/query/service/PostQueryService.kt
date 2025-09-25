package ink.radiant.query.service

import ink.radiant.core.domain.model.Post
import ink.radiant.core.domain.model.PostAuthor
import ink.radiant.core.domain.model.PostConnection
import ink.radiant.core.domain.model.PostParticipantConnection

interface PostQueryService {
    fun findPosts(first: Int?, after: String?): PostConnection
    fun findPostById(id: String): Post?
    fun findPostsByIds(ids: List<String>): List<Post>
    fun findParticipants(postId: String, first: Int?, after: String?): PostParticipantConnection
    fun findAuthor(authorId: String): PostAuthor?
}
