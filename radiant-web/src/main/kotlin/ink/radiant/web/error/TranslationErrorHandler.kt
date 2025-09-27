package ink.radiant.web.error

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import ink.radiant.core.domain.exception.TranslationException
import ink.radiant.core.domain.model.TranslationErrorType
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.graphql.execution.ErrorType
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
class TranslationErrorHandler : DataFetcherExceptionResolverAdapter() {

    override fun resolveToSingleError(exception: Throwable, environment: DataFetchingEnvironment): GraphQLError? {
        val cause = unwrap(exception)
        return when (cause) {
            is TranslationException -> buildGraphQLError(environment, cause)
            is AccessDeniedException -> buildAccessDeniedError(environment, cause)
            else -> null
        }
    }

    private fun buildGraphQLError(environment: DataFetchingEnvironment, exception: TranslationException): GraphQLError {
        return GraphqlErrorBuilder.newError(environment)
            .message(exception.message ?: GENERIC_MESSAGE)
            .errorType(ErrorType.BAD_REQUEST)
            .extensions(
                mapOf(
                    "type" to exception.errorType.name,
                    "code" to mapErrorCode(exception.errorType),
                ),
            )
            .build()
    }

    private fun buildAccessDeniedError(
        environment: DataFetchingEnvironment,
        exception: AccessDeniedException,
    ): GraphQLError {
        return GraphqlErrorBuilder.newError(environment)
            .message(exception.message ?: GENERIC_MESSAGE)
            .errorType(ErrorType.UNAUTHORIZED)
            .extensions(
                mapOf(
                    "type" to TranslationErrorType.UNAUTHORIZED.name,
                    "code" to mapErrorCode(TranslationErrorType.UNAUTHORIZED),
                ),
            )
            .build()
    }

    private fun unwrap(throwable: Throwable): Throwable {
        var current = throwable
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current
    }

    private fun mapErrorCode(errorType: TranslationErrorType): String {
        return when (errorType) {
            TranslationErrorType.TEXT_TOO_LONG -> "TRANSLATION_TEXT_TOO_LONG"
            TranslationErrorType.UNAUTHORIZED -> "TRANSLATION_UNAUTHORIZED"
            TranslationErrorType.SERVICE_UNAVAILABLE -> "TRANSLATION_SERVICE_UNAVAILABLE"
            TranslationErrorType.TIMEOUT -> "TRANSLATION_TIMEOUT"
            TranslationErrorType.RATE_LIMITED -> "TRANSLATION_RATE_LIMITED"
            TranslationErrorType.INVALID_INPUT -> "TRANSLATION_INVALID_INPUT"
            TranslationErrorType.THREAD_POOL_EXHAUSTED -> "TRANSLATION_THREAD_POOL_EXHAUSTED"
            TranslationErrorType.UNKNOWN_ERROR -> "TRANSLATION_UNKNOWN_ERROR"
        }
    }

    companion object {
        private const val GENERIC_MESSAGE = "Translation request failed"
    }
}
