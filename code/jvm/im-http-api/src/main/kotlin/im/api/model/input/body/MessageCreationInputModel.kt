package im.api.model.input.body

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Input model for creating a message.
 *
 * @property content The content of the message.
 */
class MessageCreationInputModel(
    @field:NotBlank(message = "Message content is required")
    @field:Size(min = 1, max = 300, message = "Message content must be between 1 and 300 characters")
    @field:NotNull(message = "Message content is required")
    val content: String,
)
