package im.messages

import im.channel.Channel
import im.user.User
import im.wrappers.Identifier
import im.wrappers.toIdentifier
import java.time.LocalDateTime

data class Message(
    val id: Identifier = Identifier(0),
    val channel: Channel,
    val user: User,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val editedAt: LocalDateTime? = null
) {
    companion object {
        operator fun invoke(
            id: Long = 0,
            channel: Channel,
            user: User,
            content: String,
            createdAt: LocalDateTime = LocalDateTime.now(),
            editedAt: LocalDateTime? = null
        ): Message {
            return Message(
                id = id.toIdentifier(),
                channel = channel,
                user = user,
                content = content,
                createdAt = createdAt,
                editedAt = editedAt
            )
        }
    }

    init {
        require(content.isNotBlank()) { "Message content cannot be blank" }
        require(content.length in 1..300) { "Message content must be between 1 and 300 characters" }
    }

    fun edit(content: String): Message = copy(content = content, editedAt = LocalDateTime.now())
}