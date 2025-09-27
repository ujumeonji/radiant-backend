package ink.radiant.infrastructure.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "post_participants")
class PostParticipantEntity(
    @Id
    @Column(name = "id")
    var id: String = UUID.randomUUID().toString(),
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    lateinit var post: PostEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    lateinit var profile: ProfileEntity

    constructor(post: PostEntity, profile: ProfileEntity) : this() {
        this.post = post
        this.profile = profile
    }
}
