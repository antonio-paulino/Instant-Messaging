package messages

import channel.Channel
import user.User
import java.time.LocalDateTime

data class Message(
    val id: Long = 0,
    val channel: Channel,
    val user: User,
    val content: String,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime? = null
) {
    fun edit(content: String): Message = copy(content = content, editedAt = LocalDateTime.now())
}