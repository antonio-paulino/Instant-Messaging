package im.repository.mem.model.message

import im.domain.messages.Message
import im.repository.mem.model.user.UserDTO
import java.time.LocalDateTime

data class MessageDTO(
    val id: Long,
    val channelId: Long,
    val user: UserDTO,
    val content: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val editedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromDomain(message: Message): MessageDTO =
            MessageDTO(
                id = message.id.value,
                channelId = message.channelId.value,
                user = UserDTO.fromDomain(message.user),
                content = message.content,
                createdAt = message.createdAt,
                editedAt = message.editedAt,
            )
    }

    fun toDomain(): Message =
        Message(
            id = id,
            channelId = channelId,
            user = user.toDomain(),
            content = content,
            createdAt = createdAt,
            editedAt = editedAt,
        )
}
