package ink.radiant.core.domain.model

import ink.radiant.core.domain.aggregate.AggregateId
import java.util.UUID

class TranslationSessionId private constructor(value: String) : AggregateId(value) {

    companion object {
        private const val PREFIX = "trans"

        fun from(value: String): TranslationSessionId {
            require(value.isNotBlank()) { "TranslationSessionId cannot be blank" }
            return TranslationSessionId(value)
        }

        fun generate(): TranslationSessionId {
            return TranslationSessionId("$PREFIX-${UUID.randomUUID()}")
        }
    }
}
