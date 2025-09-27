package ink.radiant.core.domain.model

data class SentencePair(
    val order: Int,
    val original: String,
    val translated: String,
) {
    init {
        require(order >= 0) { "Sentence order must be non-negative" }
        require(original.isNotBlank()) { "Original sentence must not be blank" }
        require(translated.isNotBlank()) { "Translated sentence must not be blank" }
    }
}
