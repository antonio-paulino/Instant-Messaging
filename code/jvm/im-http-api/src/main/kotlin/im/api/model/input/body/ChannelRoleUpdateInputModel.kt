package im.api.model.input.body

import im.api.model.input.wrappers.ChannelRole
import jakarta.validation.Valid

/**
 * Input model for updating a channel role.
 */
data class ChannelRoleUpdateInputModel(
    @Valid
    val role: ChannelRole,
) {
    constructor(role: String) : this(ChannelRole(role))

    fun toDomain() = role.toDomain()
}
