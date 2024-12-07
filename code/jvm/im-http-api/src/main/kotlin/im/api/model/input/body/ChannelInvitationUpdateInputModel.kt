package im.api.model.input.body

import im.api.model.input.validators.OneNotNull
import im.api.model.input.wrappers.ChannelRole
import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import java.time.LocalDateTime

@OneNotNull(
    fields = ["role", "expiresAt"],
    message = "Either role or expiresAt is required",
)
data class ChannelInvitationUpdateInputModel(
    @field:Valid
    val role: ChannelRole?,
    @field:Future(message = "New invitation expiration must be in the future")
    val expiresAt: LocalDateTime?,
) {
    constructor(
        role: String,
        expiresAt: LocalDateTime,
    ) : this(
        ChannelRole(role),
        expiresAt,
    )
}
