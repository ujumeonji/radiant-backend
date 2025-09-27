package ink.radiant.core.domain.entity

import ink.radiant.core.domain.aggregate.AggregateRoot
import ink.radiant.core.domain.event.AccountCreatedEvent
import ink.radiant.core.domain.event.AccountDeactivatedEvent
import ink.radiant.core.domain.event.AccountLoginEvent
import ink.radiant.core.domain.event.AccountUpdatedEvent
import jakarta.persistence.*
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "accounts")
class AccountEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),

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
) : AggregateRoot() {

    val displayName: String get() = profile?.displayName ?: email.substringBefore("@")

    val avatarUrl: String? get() = profile?.avatarUrl

    val providerName: String get() = provider.name.lowercase()

    fun updateLastLogin(ipAddress: String? = null, userAgent: String? = null) {
        val oldLoginTime = lastLoginAt
        lastLoginAt = OffsetDateTime.now()

        applyEvent(
            AccountLoginEvent(
                aggregateId = this.id,
                accountId = this.id,
                loginTime = Instant.now(),
                ipAddress = ipAddress,
                userAgent = userAgent,
            ),
        )
    }

    fun deactivate(deactivatedBy: String, reason: String? = null) {
        require(isActive) { "계정이 이미 비활성화되어 있습니다." }

        isActive = false

        applyEvent(
            AccountDeactivatedEvent(
                aggregateId = this.id,
                accountId = this.id,
                deactivatedBy = deactivatedBy,
                reason = reason,
            ),
        )
    }

    fun activate(activatedBy: String) {
        require(!isActive) { "계정이 이미 활성화되어 있습니다." }

        val oldValue = isActive
        isActive = true

        applyEvent(
            AccountUpdatedEvent(
                aggregateId = this.id,
                accountId = this.id,
                field = "isActive",
                oldValue = oldValue,
                newValue = isActive,
                updatedBy = activatedBy,
            ),
        )
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

    fun updateProfile(
        displayName: String?,
        bio: String?,
        location: String?,
        websiteUrl: String?,
        company: String?,
        updatedBy: String,
    ) {
        val currentProfile = this.profile
        require(currentProfile != null) { "프로필이 존재하지 않습니다." }

        var hasChanges = false

        displayName?.let {
            if (currentProfile.displayName != it) {
                val oldValue = currentProfile.displayName
                currentProfile.displayName = it
                hasChanges = true

                applyEvent(
                    AccountUpdatedEvent(
                        aggregateId = this.id,
                        accountId = this.id,
                        field = "profile.displayName",
                        oldValue = oldValue,
                        newValue = it,
                        updatedBy = updatedBy,
                    ),
                )
            }
        }

        bio?.let {
            if (currentProfile.bio != it) {
                val oldValue = currentProfile.bio
                currentProfile.bio = it
                hasChanges = true

                applyEvent(
                    AccountUpdatedEvent(
                        aggregateId = this.id,
                        accountId = this.id,
                        field = "profile.bio",
                        oldValue = oldValue,
                        newValue = it,
                        updatedBy = updatedBy,
                    ),
                )
            }
        }

        location?.let {
            if (currentProfile.location != it) {
                val oldValue = currentProfile.location
                currentProfile.location = it
                hasChanges = true

                applyEvent(
                    AccountUpdatedEvent(
                        aggregateId = this.id,
                        accountId = this.id,
                        field = "profile.location",
                        oldValue = oldValue,
                        newValue = it,
                        updatedBy = updatedBy,
                    ),
                )
            }
        }

        websiteUrl?.let {
            if (currentProfile.websiteUrl != it) {
                val oldValue = currentProfile.websiteUrl
                currentProfile.websiteUrl = it
                hasChanges = true

                applyEvent(
                    AccountUpdatedEvent(
                        aggregateId = this.id,
                        accountId = this.id,
                        field = "profile.websiteUrl",
                        oldValue = oldValue,
                        newValue = it,
                        updatedBy = updatedBy,
                    ),
                )
            }
        }

        company?.let {
            if (currentProfile.company != it) {
                val oldValue = currentProfile.company
                currentProfile.company = it
                hasChanges = true

                applyEvent(
                    AccountUpdatedEvent(
                        aggregateId = this.id,
                        accountId = this.id,
                        field = "profile.company",
                        oldValue = oldValue,
                        newValue = it,
                        updatedBy = updatedBy,
                    ),
                )
            }
        }
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
            require(email.isNotBlank()) { "이메일은 필수입니다." }
            require(name.isNotBlank()) { "이름은 필수입니다." }
            require(providerId.isNotBlank()) { "Provider ID는 필수입니다." }
            require(displayName.isNotBlank()) { "표시 이름은 필수입니다." }

            val accountId = UUID.randomUUID().toString()
            val account = AccountEntity(
                id = accountId,
                email = email,
                name = name,
                provider = provider,
                providerId = providerId,
            )

            account.createProfile(
                displayName = displayName,
                avatarUrl = avatarUrl,
            )

            account.applyEvent(
                AccountCreatedEvent(
                    aggregateId = accountId,
                    accountId = accountId,
                    email = email,
                    name = name,
                    provider = provider.name,
                    providerId = providerId,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                ),
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

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "AccountEntity(id=$id, email='$email', name='$name', provider=$provider)"
    }
}

enum class OAuthProvider {
    GITHUB,
    GOOGLE,
}
