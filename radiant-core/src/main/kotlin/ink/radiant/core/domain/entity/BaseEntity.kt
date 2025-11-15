package ink.radiant.core.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: OffsetDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    open var updatedAt: OffsetDateTime? = null

    @Column(name = "deleted_at")
    open var deletedAt: OffsetDateTime? = null

    fun softDelete() {
        this.deletedAt = OffsetDateTime.now()
    }

    fun isDeleted(): Boolean = deletedAt != null
}
