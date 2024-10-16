package im.domain.messages

import im.domain.Failure
import im.domain.Success
import im.domain.channel.Channel
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Represents a message in a channel.
 *
 * @property id The unique identifier of the message.
 * @property channel The channel where the message was sent.
 * @property user The user that sent the message.
 * @property content The content of the message.
 * @property createdAt The date and time when the message was sent.
 * @property editedAt The date and time when the message was last edited.
 */
data class Message(
    val id: Identifier = Identifier(0),
    val channel: Channel,
    val user: User,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    val editedAt: LocalDateTime? = null,
) {
    constructor(
        id: Long = 0,
        channel: Channel,
        user: User,
        content: String,
        createdAt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        editedAt: LocalDateTime? = null,
    ) : this(
        id = Identifier(id),
        channel = channel,
        user = user,
        content = content,
        createdAt = createdAt,
        editedAt = editedAt,
    )

    companion object {
        private val validator = MessageValidator()
    }

    init {
        val validation = validator.validate(content)
        require(validation is Success) { (validation as Failure).value.toErrorMessage() }
    }

    /**
     * Edits the content of the message.
     *
     * @param content the new content of the message
     * @return a new message with the updated content
     */
    fun edit(content: String): Message = copy(content = content, editedAt = LocalDateTime.now())
}
