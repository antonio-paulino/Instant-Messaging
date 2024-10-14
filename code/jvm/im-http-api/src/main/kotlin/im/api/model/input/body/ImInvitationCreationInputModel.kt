package im.api.model.input.body

import jakarta.validation.constraints.Future
import java.time.LocalDateTime

/**
 * Input model for creating an invitation to the application.
 *
 * @property expiresAt The expiration date of the invitation.
 */
data class ImInvitationCreationInputModel(
    @field:Future(message = "Invitation expiration must be in the future")
    val expiresAt: LocalDateTime,
)
