package ink.radiant.command.service

import ink.radiant.core.domain.entity.PostEntity

interface PostCommandService {
    fun createPost(title: String, body: String?, authorId: String, thumbnailUrl: String? = null): PostEntity
    fun updatePost(postId: String, title: String?, body: String?, updatedBy: String): PostEntity
    fun deletePost(postId: String, deletedBy: String)
    fun likePost(postId: String, likedBy: String): PostEntity
}
