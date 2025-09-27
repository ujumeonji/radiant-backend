package ink.radiant.core.domain.model

enum class TranslationErrorType {
    SERVICE_UNAVAILABLE,
    INVALID_INPUT,
    TIMEOUT,
    RATE_LIMITED,
    UNAUTHORIZED,
    TEXT_TOO_LONG,
    THREAD_POOL_EXHAUSTED,
    UNKNOWN_ERROR,
}
