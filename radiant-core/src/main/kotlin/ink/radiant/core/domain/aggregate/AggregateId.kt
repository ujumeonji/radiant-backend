package ink.radiant.core.domain.aggregate

abstract class AggregateId(val value: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateId) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value
}
