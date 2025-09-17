package ink.radiant.web.exception

import ink.radiant.core.common.exception.AggregateNotFoundException
import ink.radiant.core.common.exception.ConcurrencyException
import ink.radiant.core.common.exception.DomainException
import ink.radiant.core.common.exception.EventStoreException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AggregateNotFoundException::class)
    fun handleAggregateNotFound(ex: AggregateNotFoundException): ResponseEntity<ApiError> {
        val error = ApiError(
            code = "AGGREGATE_NOT_FOUND",
            message = ex.message ?: "Aggregate not found",
            status = HttpStatus.NOT_FOUND.value(),
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(ConcurrencyException::class)
    fun handleConcurrencyException(ex: ConcurrencyException): ResponseEntity<ApiError> {
        val error = ApiError(
            code = "CONCURRENCY_CONFLICT",
            message = ex.message ?: "Concurrency conflict occurred",
            status = HttpStatus.CONFLICT.value(),
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(EventStoreException::class)
    fun handleEventStoreException(ex: EventStoreException): ResponseEntity<ApiError> {
        val error = ApiError(
            code = "EVENT_STORE_ERROR",
            message = "An error occurred while processing the request",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException): ResponseEntity<ApiError> {
        val error = ApiError(
            code = "DOMAIN_ERROR",
            message = ex.message ?: "Domain error occurred",
            status = HttpStatus.BAD_REQUEST.value(),
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiError> {
        val error = ApiError(
            code = "INVALID_ARGUMENT",
            message = ex.message ?: "Invalid argument provided",
            status = HttpStatus.BAD_REQUEST.value(),
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiError> {
        val error = ApiError(
            code = "INTERNAL_ERROR",
            message = "An unexpected error occurred",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
