package ink.radiant.eventstore.handler

import ink.radiant.core.domain.event.PostCreatedEvent
import ink.radiant.core.domain.event.PostDeletedEvent
import ink.radiant.core.domain.event.PostLikedEvent
import ink.radiant.core.domain.event.PostUpdatedEvent
import ink.radiant.infrastructure.repository.PostRepository
import ink.radiant.infrastructure.repository.PostViewRepository
import ink.radiant.infrastructure.view.PostViewEntity
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PostEventHandler(
    private val postRepository: PostRepository,
    private val postViewRepository: PostViewRepository,
) {

    private val logger = LoggerFactory.getLogger(PostEventHandler::class.java)

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostCreated(event: PostCreatedEvent) {
        try {
            logger.info("Processing PostCreatedEvent for postId: ${event.postId}")

            val postEntity = postRepository.findById(event.postId)
                .orElseThrow { IllegalArgumentException("Post not found: ${event.postId}") }

            val postView = PostViewEntity.fromPostEntity(postEntity)
            postViewRepository.save(postView)

            logger.info("Successfully created post view for postId: ${event.postId}")
        } catch (e: Exception) {
            logger.error("Failed to handle PostCreatedEvent for postId: ${event.postId}", e)
            throw e
        }
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostUpdated(event: PostUpdatedEvent) {
        try {
            logger.info("Processing PostUpdatedEvent for postId: ${event.postId}, field: ${event.field}")

            val postView = postViewRepository.findById(event.postId)
                .orElseThrow { IllegalArgumentException("Post view not found: ${event.postId}") }

            when (event.field) {
                "title" -> {
                    postView.title = event.newValue as String
                }
                "body" -> {
                    postView.body = event.newValue as String?
                }
                "translatedTitle" -> {
                    postView.translatedTitle = event.newValue as String?
                }
            }

            postViewRepository.save(postView)

            logger.info("Successfully updated post view for postId: ${event.postId}, field: ${event.field}")
        } catch (e: Exception) {
            logger.error("Failed to handle PostUpdatedEvent for postId: ${event.postId}", e)
            throw e
        }
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostLiked(event: PostLikedEvent) {
        try {
            logger.info("Processing PostLikedEvent for postId: ${event.postId}")

            val postView = postViewRepository.findById(event.postId)
                .orElseThrow { IllegalArgumentException("Post view not found: ${event.postId}") }

            postView.likes = event.totalLikes
            postViewRepository.save(postView)

            logger.info("Successfully updated likes for postId: ${event.postId}, totalLikes: ${event.totalLikes}")
        } catch (e: Exception) {
            logger.error("Failed to handle PostLikedEvent for postId: ${event.postId}", e)
            throw e
        }
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostDeleted(event: PostDeletedEvent) {
        try {
            logger.info("Processing PostDeletedEvent for postId: ${event.postId}")

            val postView = postViewRepository.findById(event.postId)
                .orElseThrow { IllegalArgumentException("Post view not found: ${event.postId}") }

            postView.softDelete()
            postViewRepository.save(postView)

            logger.info("Successfully soft deleted post view for postId: ${event.postId}")
        } catch (e: Exception) {
            logger.error("Failed to handle PostDeletedEvent for postId: ${event.postId}", e)
            throw e
        }
    }
}
