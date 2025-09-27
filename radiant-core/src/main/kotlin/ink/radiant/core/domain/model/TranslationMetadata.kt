package ink.radiant.core.domain.model

data class TranslationMetadata(
    val processingTimeMs: Long,
    val tokenCount: Int,
    val chunkCount: Int,
) {
    init {
        require(processingTimeMs >= 0) { "Processing time must be non-negative" }
        require(tokenCount >= 0) { "Token count must be non-negative" }
        require(chunkCount > 0) { "Chunk count must be greater than zero" }
    }
}
