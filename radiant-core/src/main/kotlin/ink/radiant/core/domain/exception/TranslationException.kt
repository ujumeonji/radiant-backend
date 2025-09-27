package ink.radiant.core.domain.exception

import ink.radiant.core.domain.model.TranslationErrorType

open class TranslationException(
    val errorType: TranslationErrorType,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class TranslationInputException(message: String) : TranslationException(
    errorType = TranslationErrorType.INVALID_INPUT,
    message = message,
)

class TranslationLengthExceededException(
    actualLength: Int,
    maxLength: Int,
) : TranslationException(
    errorType = TranslationErrorType.TEXT_TOO_LONG,
    message = "Input text exceeds maximum length of $maxLength characters (actual: $actualLength)",
)

class TranslationUnauthorizedException(message: String = "Admin role required for translation access") :
    TranslationException(
        errorType = TranslationErrorType.UNAUTHORIZED,
        message = message,
    )

class TranslationServiceUnavailableException(
    message: String = "Translation service is temporarily unavailable",
    cause: Throwable? = null,
) : TranslationException(
    errorType = TranslationErrorType.SERVICE_UNAVAILABLE,
    message = message,
    cause = cause,
)

class TranslationTimeoutException(message: String = "Translation request timed out") : TranslationException(
    errorType = TranslationErrorType.TIMEOUT,
    message = message,
)

class TranslationRateLimitedException(message: String = "Translation provider rate limit reached") :
    TranslationException(
        errorType = TranslationErrorType.RATE_LIMITED,
        message = message,
    )

class TranslationThreadPoolExhaustedException(message: String = "Translation executor is at capacity") :
    TranslationException(
        errorType = TranslationErrorType.THREAD_POOL_EXHAUSTED,
        message = message,
    )

class TranslationUnknownException(
    message: String = "Unknown translation failure",
    cause: Throwable? = null,
) : TranslationException(
    errorType = TranslationErrorType.UNKNOWN_ERROR,
    message = message,
    cause = cause,
)
