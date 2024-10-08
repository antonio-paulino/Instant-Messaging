package im.messages

import im.channel.Channel
import im.user.User
import java.time.LocalDateTime

data class Message(
    val id: Long = 0,
    val channel: Channel,
    val user: User,
    val content: String,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime? = null
) {
    init {
        require(id >= 0) { "Message ID must be positive" }
        require(content.isNotBlank()) { "Message content cannot be blank" }
        require(content.length in 1..300) { "Message content must be between 1 and 300 characters" }
    }
    fun edit(content: String): Message = copy(content = content, editedAt = LocalDateTime.now())
}