package im.model.problems

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

sealed class Problem(
    typeURI: URI,
) {
    private val type = typeURI.toString()
    private val title: String = typeURI.path.split("/").last()

    fun response(status: HttpStatus, detail: String): ResponseEntity<Any> =
        ResponseEntity.status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(
                OutputModel(
                    type,
                    title,
                    status.value(),
                    detail,
                )
            )

    private inner class OutputModel(
        val type: String,
        val title: String,
        val status: Int,
        val detail: String,
    ) {
        override fun toString(): String {
            return """
                {
                    "type": "$type",
                    "title": "$title",
                    "status": $status,
                    "detail": "$detail"
                }
            """.trimIndent()
        }
    }

    data object InvalidInvitationProblem :
        Problem(URI.create("https://github.com/isel-leic-daw/2024-daw-leic52d-im-i52d-2425-g07/tree/main/docs/problems/invalid-invitation"))

    data object UnauthorizedProblem :
        Problem(URI.create("https://github.com/isel-leic-daw/2024-daw-leic52d-im-i52d-2425-g07/tree/main/docs/problems/unauthorized"))

    data object UserAlreadyExistsProblem :
        Problem(URI.create("https://github.com/isel-leic-daw/2024-daw-leic52d-im-i52d-2425-g07/tree/main/docs/problems/user-already-exists"))
}