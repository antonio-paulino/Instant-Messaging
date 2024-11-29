package im.services.messages

import im.domain.Either
import im.domain.Failure
import im.domain.channel.ChannelRole
import im.domain.messages.Message
import im.domain.success
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import jakarta.inject.Named
import java.time.LocalDateTime

@Named
class MessageServiceImpl(
    private val transactionManager: TransactionManager,
) : MessageService {
    companion object {
        private const val DEFAULT_SORT = "createdAt"
        private val validSortFields = setOf("id", "createdAt", "editedAt")
    }

    override fun getChannelMessages(
        channelId: Identifier,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
        before: LocalDateTime?,
        user: User,
    ): Either<MessageError, Pagination<Message>> =
        transactionManager.run {
            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(MessageError.ChannelNotFound)

            channelRepository.getMember(channel, user)
                ?: return@run Failure(MessageError.UserNotInChannel)

            val sort = sortRequest.sortBy ?: DEFAULT_SORT

            if (sort !in validSortFields) {
                return@run Failure(MessageError.InvalidSortField(sort, validSortFields.toList()))
            }

            val messages =
                messageRepository.findByChannel(
                    channel,
                    pagination,
                    sortRequest.copy(sortBy = sort),
                    before ?: LocalDateTime.now(),
                )

            success(messages)
        }

    override fun createMessage(
        channelId: Identifier,
        message: String,
        user: User,
    ): Either<MessageError, Message> =
        transactionManager.run {
            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(MessageError.ChannelNotFound)

            val (_, role) =
                channelRepository.getMember(channel, user)
                    ?: return@run Failure(MessageError.UserNotInChannel)

            if (role == ChannelRole.GUEST) {
                return@run Failure(MessageError.NoWritePermission)
            }

            val message = messageRepository.save(Message(0L, channelId = channel.id.value, user = user, content = message))

            success(message)
        }

    override fun updateMessage(
        channelId: Identifier,
        messageId: Identifier,
        message: String,
        user: User,
    ): Either<MessageError, Message> =
        transactionManager.run {
            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(MessageError.ChannelNotFound)

            channelRepository.getMember(channel, user)
                ?: return@run Failure(MessageError.UserNotInChannel)

            val msg =
                messageRepository.findByChannelAndId(channel, messageId)
                    ?: return@run Failure(MessageError.MessageNotFound)

            if (msg.user != user) {
                return@run Failure(MessageError.CannotEditMessage)
            }

            val newMessage = msg.edit(message)

            messageRepository.save(newMessage)

            success(newMessage)
        }

    override fun deleteMessage(
        channelId: Identifier,
        messageId: Identifier,
        user: User,
    ): Either<MessageError, Unit> =
        transactionManager.run {
            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(MessageError.ChannelNotFound)

            val (_, role) =
                channelRepository.getMember(channel, user)
                    ?: return@run Failure(MessageError.UserNotInChannel)

            val msg =
                messageRepository.findByChannelAndId(channel, messageId)
                    ?: return@run Failure(MessageError.MessageNotFound)

            if (msg.user != user && role != ChannelRole.OWNER) {
                return@run Failure(MessageError.CannotDeleteMessage)
            }

            messageRepository.delete(msg)

            success(Unit)
        }

    override fun getMessageById(
        channelId: Identifier,
        messageId: Identifier,
        user: User,
    ): Either<MessageError, Message> {
        return transactionManager.run {
            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(MessageError.ChannelNotFound)

            channelRepository.getMember(channel, user)
                ?: return@run Failure(MessageError.UserNotInChannel)

            val message =
                messageRepository.findByChannelAndId(channel, messageId)
                    ?: return@run Failure(MessageError.MessageNotFound)

            success(message)
        }
    }
}
