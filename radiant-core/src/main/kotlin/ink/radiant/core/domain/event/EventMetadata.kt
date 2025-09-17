package ink.radiant.core.domain.event

data class EventMetadata(
    val userId: String? = null,
    val correlationId: String? = null,
    val causationId: String? = null,
    val additionalData: Map<String, Any> = emptyMap(),
)
