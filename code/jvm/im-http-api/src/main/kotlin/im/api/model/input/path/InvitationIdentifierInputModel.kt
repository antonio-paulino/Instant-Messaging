package im.api.model.input.path

import im.api.model.input.wrappers.Identifier
import jakarta.validation.Valid

/**
 * Input model for invitation identifier in path.
 */
data class InvitationIdentifierInputModel(
    @field:Valid
    val invitationId: Identifier,
) {
    fun toDomain() = invitationId.toDomain()
}
