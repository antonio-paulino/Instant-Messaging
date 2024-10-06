package im.model.input.body

import jakarta.validation.constraints.Future
import java.time.LocalDateTime

data class ImInvitationInputModel(
    @field:Future(message = "Invitation expiration must be in the future")
    val expiration: LocalDateTime
)
