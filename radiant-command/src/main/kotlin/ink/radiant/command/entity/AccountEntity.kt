package ink.radiant.command.entity

import ink.radiant.infrastructure.share.BaseEntity
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "accounts")
class AccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: OAuthProvider,

    @Column(name = "provider_id", nullable = false)
    val providerId: String,

    @Column(name = "last_login_at")
    var lastLoginAt: OffsetDateTime? = null,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @OneToOne(mappedBy = "account", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var profile: ProfileEntity? = null,
) : BaseEntity() {

    val displayName: String get() = profile?.displayName ?: email.substringBefore("@")

    val avatarUrl: String? get() = profile?.avatarUrl

    val providerName: String get() = provider.name.lowercase()

    fun updateLastLogin() {
        lastLoginAt = OffsetDateTime.now()
    }

    fun deactivate() {
        isActive = false
    }

    fun activate() {
        isActive = true
    }

    private fun createProfile(
        displayName: String,
        avatarUrl: String? = null,
        bio: String? = null,
        location: String? = null,
        websiteUrl: String? = null,
        company: String? = null,
        professionalFields: MutableSet<ProfessionalField> = mutableSetOf(),
    ): ProfileEntity {
        if (profile != null) {
            throw IllegalStateException("Profile already exists for this account")
        }

        val newProfile = ProfileEntity(
            account = this,
            displayName = displayName,
            avatarUrl = avatarUrl,
            bio = bio,
            location = location,
            websiteUrl = websiteUrl,
            company = company,
            professionalFields = professionalFields,
        )

        this.profile = newProfile
        return newProfile
    }

    companion object {
        fun signUp(
            email: String,
            name: String,
            provider: OAuthProvider,
            providerId: String,
            displayName: String,
            avatarUrl: String? = null,
        ): AccountEntity {
            val account = AccountEntity(
                email = email,
                name = name,
                provider = provider,
                providerId = providerId,
            )

            account.createProfile(
                displayName = displayName,
                avatarUrl = avatarUrl,
            )

            return account
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "AccountEntity(id=$id, email='$email', name='$name', provider=$provider)"
    }
}

enum class OAuthProvider {
    GITHUB,
    GOOGLE,
}
