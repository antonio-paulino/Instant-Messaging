package im.api.model.output.messages

import im.api.model.output.users.UserOutputModel
import im.domain.messages.Message
import java.time.LocalDateTime

data class MessageOutputModel(
    val id: Long,
    val channelId: Long,
    val author: UserOutputModel,
    val content: String,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime?,
) {
    companion object {
        fun fromDomain(message: Message): MessageOutputModel =
            MessageOutputModel(
                id = message.id.value,
                channelId = message.channelId.value,
                author = UserOutputModel.fromDomain(message.user),
                content = message.content,
                createdAt = message.createdAt,
                editedAt = message.editedAt,
            )
    }
}
