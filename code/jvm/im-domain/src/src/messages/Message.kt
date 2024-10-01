package messages

import channel.Channel
import user.User
import java.time.LocalDateTime

interface Message {
    val id: Long
    val author: User
    val channel: Channel
    val content: String
    val createdAt: LocalDateTime
    val editedAt: LocalDateTime

    fun edit(content: String) : Message
}