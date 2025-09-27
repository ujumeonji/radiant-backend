package ink.radiant.infrastructure.view

import ink.radiant.core.domain.entity.AccountEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "account_views")
class AccountViewEntity(
    @Id
    @Column(name = "id")
    var id: String = "",

    @Column(name = "email", unique = true, nullable = false)
    var email: String = "",

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "provider", nullable = false)
    var provider: String = "",

    @Column(name = "provider_id", nullable = false)
    var providerId: String = "",

    @Column(name = "display_name")
    var displayName: String = "",

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "bio")
    var bio: String? = null,

    @Column(name = "location")
    var location: String? = null,

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    @Column(name = "company")
    var company: String? = null,

    @Column(name = "posts_count", nullable = false)
    var postsCount: Int = 0,

    @Column(name = "views_count", nullable = false)
    var viewsCount: Long = 0L,

    @Column(name = "followers_count", nullable = false)
    var followersCount: Int = 0,

    @Column(name = "following_count", nullable = false)
    var followingCount: Int = 0,

    @Column(name = "last_login_at")
    var lastLoginAt: OffsetDateTime? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    constructor() : this(
        email = "",
        name = "",
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountViewEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun fromAccountEntity(accountEntity: AccountEntity): AccountViewEntity {
            val profile = accountEntity.profile

            return AccountViewEntity(
                id = accountEntity.id,
                email = accountEntity.email,
                name = accountEntity.name,
                provider = accountEntity.provider.name.lowercase(),
                providerId = accountEntity.providerId,
                displayName = profile?.displayName ?: accountEntity.displayName,
                avatarUrl = profile?.avatarUrl,
                bio = profile?.bio,
                location = profile?.location,
                websiteUrl = profile?.websiteUrl,
                company = profile?.company,
                postsCount = profile?.postsCount ?: 0,
                viewsCount = profile?.viewsCount ?: 0L,
                followersCount = profile?.followersCount ?: 0,
                followingCount = profile?.followingCount ?: 0,
                lastLoginAt = accountEntity.lastLoginAt,
                isActive = accountEntity.isActive,
                createdAt = accountEntity.createdAt ?: OffsetDateTime.now(),
                updatedAt = accountEntity.updatedAt ?: OffsetDateTime.now(),
            )
        }
    }
}
