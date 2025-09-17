package ink.radiant.web.exception

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

data class ApiError(
    val code: String,
    val message: String,
    val status: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val timestamp: Instant = Instant.now(),
    val details: Map<String, Any>? = null,
)

abstract class ApiException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
