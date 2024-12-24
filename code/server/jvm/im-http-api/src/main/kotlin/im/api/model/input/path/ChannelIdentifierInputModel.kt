package im.api.model.input.path

import im.api.model.input.wrappers.Identifier
import jakarta.validation.Valid

/**
 * Input model for channel identifier in path.
 */
data class ChannelIdentifierInputModel(
    @field:Valid
    val channelId: Identifier,
) {
    fun toDomain() = channelId.toDomain()
}
