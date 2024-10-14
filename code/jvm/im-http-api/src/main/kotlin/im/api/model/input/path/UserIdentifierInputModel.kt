package im.api.model.input.path

import im.api.model.input.wrappers.Identifier
import jakarta.validation.Valid

/**
 * Input model for message identifier in path.
 */
data class UserIdentifierInputModel(
    @field:Valid
    val userId: Identifier,
) {
    fun toDomain() = userId.toDomain()
}
