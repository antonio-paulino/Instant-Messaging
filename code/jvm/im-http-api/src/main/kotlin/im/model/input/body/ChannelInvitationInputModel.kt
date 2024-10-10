package im.model.input.body

import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import im.model.input.wrappers.ChannelRole
import im.model.input.wrappers.Identifier
import java.time.LocalDateTime

class ChannelInvitationInputModel(
    @field:NotBlank
    @Valid
    val userId: Identifier,

    @field:Future
    val expiration: LocalDateTime,

    @Valid
    val role: ChannelRole
)