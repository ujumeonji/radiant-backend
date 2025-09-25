package ink.radiant.command.service

import ink.radiant.core.domain.event.PostViewedEvent
import ink.radiant.core.domain.model.PageInfo
import ink.radiant.core.domain.model.Post
import ink.radiant.core.domain.model.PostAuthor
import ink.radiant.core.domain.model.PostConnection
import ink.radiant.core.domain.model.PostEdge
import ink.radiant.core.domain.model.PostParticipant
import ink.radiant.core.domain.model.PostParticipantConnection
import ink.radiant.core.domain.model.PostParticipantEdge
import ink.radiant.infrastructure.entity.PostParticipantEntity
import ink.radiant.infrastructure.mapper.PostQueryMapper
import ink.radiant.infrastructure.messaging.EventPublisher
import ink.radiant.infrastructure.repository.AccountRepository
import ink.radiant.infrastructure.repository.PostParticipantRepository
import ink.radiant.query.service.PostQueryService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.Base64

@Service
@Transactional
class PostServiceImpl(
    private val postQueryMapper: PostQueryMapper,
    private val eventPublisher: EventPublisher,
    private val postParticipantRepository: PostParticipantRepository,
    private val accountRepository: AccountRepository,
) : PostCommandService, PostQueryService {

    override fun findPosts(first: Int?, after: String?): PostConnection {
        val limit = (first ?: 10).coerceIn(1, 100)
        val actualLimit = limit + 1

        val posts = if (after != null) {
            val cursor = decodeCursor(after)
            postQueryMapper.findAllAfterCursor(cursor, actualLimit)
        } else {
            postQueryMapper.findAllOrderByCreatedAtDesc(actualLimit)
        }

        val hasNextPage = posts.size > limit
        val resultPosts = if (hasNextPage) posts.dropLast(1) else posts

        val edges = resultPosts.map { postQueryModel ->
            PostEdge(
                post = postQueryModel.toDomainModel(),
                cursor = encodeCursor(postQueryModel.createdAt),
            )
        }

        val pageInfo = PageInfo(
            hasNextPage = hasNextPage,
            hasPreviousPage = after != null,
            startCursor = edges.firstOrNull()?.cursor,
            endCursor = edges.lastOrNull()?.cursor,
        )

        val totalCount = postQueryMapper.countExistingPosts().toInt()

        return PostConnection(
            edges = edges,
            pageInfo = pageInfo,
            totalCount = totalCount,
        )
    }

    override fun findPostById(id: String): Post? {
        val post = postQueryMapper.findByIdAndNotDeleted(id)?.toDomainModel()

        if (post != null) {
            val event = PostViewedEvent(
                aggregateId = id,
                postId = id,
            )
            eventPublisher.publish(event)
        }

        return post
    }

    override fun findPostsByIds(ids: List<String>): List<Post> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        return postQueryMapper.findByIdsAndNotDeleted(ids)
            .map { it.toDomainModel() }
    }

    override fun findParticipants(postId: String, first: Int?, after: String?): PostParticipantConnection {
        val limit = (first ?: DEFAULT_PARTICIPANT_LIMIT).coerceIn(1, MAX_PARTICIPANT_LIMIT)
        val totalCount = postParticipantRepository.countActiveByPostId(postId).toInt()
        if (totalCount == 0) {
            return emptyParticipantConnection()
        }

        val anchor = after?.let { decodeParticipantCursor(it) }
        val pageable = PageRequest.of(0, limit + 1)
        val participants = postParticipantRepository.findActiveByPostId(
            postId = postId,
            cursorCreatedAt = anchor?.createdAt,
            cursorId = anchor?.id,
            pageable = pageable,
        )

        if (participants.isEmpty()) {
            return emptyParticipantConnection()
        }

        val hasNextPage = participants.size > limit
        val visibleParticipants = if (hasNextPage) participants.dropLast(1) else participants

        val edges = visibleParticipants.map { entity ->
            PostParticipantEdge(
                participant = entity.toDomainParticipant(),
                cursor = encodeParticipantCursor(entity),
            )
        }

        val pageInfo = PageInfo(
            hasNextPage = hasNextPage,
            hasPreviousPage = after != null,
            startCursor = edges.firstOrNull()?.cursor,
            endCursor = edges.lastOrNull()?.cursor,
        )

        return PostParticipantConnection(
            edges = edges,
            pageInfo = pageInfo,
            totalCount = totalCount,
        )
    }

    override fun findAuthor(authorId: String): PostAuthor? {
        val account = accountRepository.findById(authorId).orElse(null) ?: return null
        val profile = account.profile

        val joinedAt = account.createdAt ?: profile?.createdAt ?: OffsetDateTime.now()

        return if (profile != null) {
            PostAuthor(
                id = account.id,
                username = account.name,
                name = profile.displayName,
                avatarUrl = profile.avatarUrl,
                bio = profile.bio,
                location = profile.location,
                websiteUrl = profile.websiteUrl,
                joinedAt = joinedAt,
                postsCount = profile.postsCount,
                viewsCount = profile.viewsCount,
                followersCount = profile.followersCount,
                followingCount = profile.followingCount,
                professionalFields = profile.professionalFields.map { it.name }.toSet(),
            )
        } else {
            PostAuthor(
                id = account.id,
                username = account.name,
                name = account.displayName,
                avatarUrl = account.avatarUrl,
                bio = null,
                location = null,
                websiteUrl = null,
                joinedAt = joinedAt,
                postsCount = 0,
                viewsCount = 0,
                followersCount = 0,
                followingCount = 0,
                professionalFields = emptySet(),
            )
        }
    }

    private fun encodeCursor(dateTime: OffsetDateTime): String {
        return Base64.getEncoder().encodeToString(dateTime.toString().toByteArray(StandardCharsets.UTF_8))
    }

    private fun decodeCursor(cursor: String): OffsetDateTime {
        val decoded = String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8)
        return OffsetDateTime.parse(decoded)
    }

    private fun encodeParticipantCursor(entity: PostParticipantEntity): String {
        val createdAt = entity.safeCreatedAt()
        val rawCursor = "$createdAt|${entity.id}"
        return Base64.getEncoder().encodeToString(rawCursor.toByteArray(StandardCharsets.UTF_8))
    }

    private fun decodeParticipantCursor(cursor: String): ParticipantCursor {
        val decoded = String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8)
        val parts = decoded.split("|")
        require(parts.size == 2) { "Invalid cursor format" }
        val createdAt = OffsetDateTime.parse(parts[0])
        val id = parts[1]
        return ParticipantCursor(createdAt = createdAt, id = id)
    }

    private fun emptyParticipantConnection(): PostParticipantConnection = PostParticipantConnection(
        edges = emptyList(),
        pageInfo = PageInfo(
            hasNextPage = false,
            hasPreviousPage = false,
            startCursor = null,
            endCursor = null,
        ),
        totalCount = 0,
    )

    private fun PostParticipantEntity.toDomainParticipant(): PostParticipant {
        val profile = this.profile
        val account = profile.account
        val joinedAt = account.createdAt ?: profile.createdAt ?: OffsetDateTime.now()

        return PostParticipant(
            id = account.id,
            username = account.name,
            name = profile.displayName,
            avatarUrl = profile.avatarUrl,
            bio = profile.bio,
            location = profile.location,
            websiteUrl = profile.websiteUrl,
            joinedAt = joinedAt,
            postsCount = profile.postsCount,
            viewsCount = profile.viewsCount,
            followersCount = profile.followersCount,
            followingCount = profile.followingCount,
            professionalFields = profile.professionalFields.map { it.name }.toSet(),
        )
    }

    private fun PostParticipantEntity.safeCreatedAt(): OffsetDateTime =
        this.createdAt ?: this.updatedAt ?: OffsetDateTime.MIN

    private data class ParticipantCursor(
        val createdAt: OffsetDateTime,
        val id: String,
    )

    companion object {
        private const val DEFAULT_PARTICIPANT_LIMIT = 10
        private const val MAX_PARTICIPANT_LIMIT = 50
    }
}
