package im.services.messages

import im.domain.Either
import im.domain.messages.Message
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import java.time.LocalDateTime

interface MessageService {
    /**
     * Retrieves the messages of a channel.
     *
     * A user must be a member of the channel to retrieve its messages.
     *
     * @param channelId the unique identifier of the channel
     * @param pagination the pagination settings
     * @param user the user that is requesting the messages
     * @param sortRequest the sort settings
     * @return a list of messages
     */
    fun getChannelMessages(
        channelId: Identifier,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
        user: User,
    ): Either<MessageError, Pagination<Message>>

    /**
     * Creates a message in a channel.
     *
     * A user must be a member of the channel and have write permission to create a message.
     *
     * @param channelId the unique identifier of the channel
     * @param message the content of the message
     * @param user the user that is creating the message
     * @return the created message
     */
    fun createMessage(
        channelId: Identifier,
        message: String,
        user: User,
    ): Either<MessageError, Message>

    /**
     * Updates the content of a message.
     *
     * A user must be the author of the message to update its content.
     *
     * @param channelId the unique identifier of the channel
     * @param messageId the unique identifier of the message
     * @param message the new content of the message
     * @param user the user that is updating the message
     * @return the date and time when the message was updated
     */
    fun updateMessage(
        channelId: Identifier,
        messageId: Identifier,
        message: String,
        user: User,
    ): Either<MessageError, LocalDateTime>

    /**
     * Deletes a message from a channel.
     *
     * A user must be the author of the message or the owner of the channel to delete a message.
     *
     * @param channelId the unique identifier of the channel
     * @param messageId the unique identifier of the message
     * @param user the user that is deleting the message
     * @return a unit value
     */
    fun deleteMessage(
        channelId: Identifier,
        messageId: Identifier,
        user: User,
    ): Either<MessageError, Unit>

    /**
     * Retrieves a message by its unique identifier.
     *
     * A user must be a member of the channel to retrieve the message.
     *
     * @param channelId the unique identifier of the channel
     * @param messageId the unique identifier of the message
     * @param user the user that is requesting the message
     * @return the message
     */
    fun getMessageById(
        channelId: Identifier,
        messageId: Identifier,
        user: User,
    ): Either<MessageError, Message>
}
