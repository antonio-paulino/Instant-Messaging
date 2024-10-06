package im.model.problems

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

internal const val MEDIA_TYPE = "application/problem+json"

sealed class InputValidationProblem(
    typeURI: URI,
    private val detail: String,
) {
    private val type = typeURI.toString()
    private val title: String = typeURI.path.split("/").last()

    fun response(status: HttpStatus, errors: List<String>): ResponseEntity<Any> =
        ResponseEntity.status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(
                OutputModel(
                    type,
                    title,
                    status.value(),
                    detail,
                    errors
                )
            )

    private inner class OutputModel(
        val type: String,
        val title: String,
        val status: Int,
        val detail: String,
        val errors: List<String>
    )

    data object InvalidInput :
        InputValidationProblem(
            URI("https://github.com/isel-leic-daw/2024-daw-leic52d-im-i52d-2425-g07/tree/main/docs/problems/invalid-input"),
            "Request input validation failed"
        )
}