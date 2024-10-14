package im.api.model.problems

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

internal const val MEDIA_TYPE = "application/problem+json"
internal const val PROBLEMS_URI =
    "https://github.com/isel-leic-daw/2024-daw-leic52d-im-i52d-2425-g07/tree/main/docs/problems"

/**
 * Represents a problem that occurred during input validation.
 *
 * This may happen due to invalid input data or invalid request body.
 *
 */
sealed class InputValidationProblem(
    typeURI: URI,
    private val detail: String,
) {
    private val type = typeURI.toString()
    private val title: String = typeURI.path.split("/").last()

    fun response(
        status: HttpStatus,
        errors: List<String>,
    ): ResponseEntity<Any> =
        ResponseEntity.status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(
                InputValidationProblemOutputModel(
                    type,
                    title,
                    status.value(),
                    detail,
                    errors,
                ),
            )

    data object InvalidInput :
        InputValidationProblem(
            URI("$PROBLEMS_URI/invalid-input"),
            "Request input validation failed",
        )
}
