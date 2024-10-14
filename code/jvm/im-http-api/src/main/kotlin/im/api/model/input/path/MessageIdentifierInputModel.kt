package im.api.model.input.path

import im.api.model.input.wrappers.Identifier
import jakarta.validation.Valid

/**
 * Input model for message identifier in path.
 */
data class MessageIdentifierInputModel(
    @field:Valid
    val messageId: Identifier,
) {
    fun toDomain() = messageId.toDomain()
}
