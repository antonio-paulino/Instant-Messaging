package im.api.model.output.messages

import im.domain.messages.Message
import java.time.LocalDateTime

data class MessageCreationOutputModel(
    val id: Long,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime?,
) {
    companion object {
        fun fromDomain(message: Message): MessageCreationOutputModel =
            MessageCreationOutputModel(
                id = message.id.value,
                createdAt = message.createdAt,
                editedAt = message.editedAt,
            )
    }
}
