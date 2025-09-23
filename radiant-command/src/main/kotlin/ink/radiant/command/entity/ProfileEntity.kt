package ink.radiant.command.entity

import ink.radiant.query.entity.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "profiles")
class ProfileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val account: AccountEntity,

    @Column(nullable = false)
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    var bio: String? = null,

    var location: String? = null,

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    @Column(name = "company")
    var company: String? = null,

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "profile_professional_fields",
        joinColumns = [JoinColumn(name = "profile_id")],
    )
    @Column(name = "professional_field")
    var professionalFields: MutableSet<ProfessionalField> = mutableSetOf(),

    @Column(name = "posts_count", nullable = false)
    var postsCount: Int = 0,

    @Column(name = "views_count", nullable = false)
    var viewsCount: Long = 0,

    @Column(name = "followers_count", nullable = false)
    var followersCount: Int = 0,

    @Column(name = "following_count", nullable = false)
    var followingCount: Int = 0,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = true,

    @Column(name = "email_notifications", nullable = false)
    var emailNotifications: Boolean = true,
) : BaseEntity() {

    fun updateProfile(
        displayName: String? = null,
        avatarUrl: String? = null,
        bio: String? = null,
        location: String? = null,
        websiteUrl: String? = null,
        company: String? = null,
        professionalFields: Set<ProfessionalField>? = null,
    ) {
        displayName?.let { this.displayName = it }
        avatarUrl?.let { this.avatarUrl = it }
        bio?.let { this.bio = it }
        location?.let { this.location = it }
        websiteUrl?.let { this.websiteUrl = it }
        company?.let { this.company = it }
        professionalFields?.let {
            this.professionalFields.clear()
            this.professionalFields.addAll(it)
        }
    }

    fun incrementPostsCount() {
        postsCount++
    }

    fun decrementPostsCount() {
        if (postsCount > 0) {
            postsCount--
        }
    }

    fun incrementViewsCount(views: Long = 1) {
        viewsCount += views
    }

    fun updateFollowersCount(count: Int) {
        followersCount = count.coerceAtLeast(0)
    }

    fun updateFollowingCount(count: Int) {
        followingCount = count.coerceAtLeast(0)
    }

    fun toggleVisibility() {
        isPublic = !isPublic
    }

    fun toggleEmailNotifications() {
        emailNotifications = !emailNotifications
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ProfileEntity(id=$id, displayName='$displayName', account=${account.name})"
    }
}

enum class ProfessionalField {
    BACKEND,
    FRONTEND,
    FULLSTACK,
    MOBILE,
    DEVOPS,
    AI_ML,
    DATA_SCIENCE,
    SECURITY,
    GAME_DEV,
    EMBEDDED,
    BLOCKCHAIN,
    PRODUCT_MANAGEMENT,
    DESIGN,
    QA_TESTING,
    OTHER,
}
