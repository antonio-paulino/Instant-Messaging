package im.api.controllers

import im.api.middlewares.authentication.Authenticated
import im.api.model.input.body.MessageCreationInputModel
import im.api.model.input.path.ChannelIdentifierInputModel
import im.api.model.input.path.MessageIdentifierInputModel
import im.api.model.input.query.PaginationInputModel
import im.api.model.input.query.SortInputModel
import im.api.model.output.messages.MessageCreationOutputModel
import im.api.model.output.messages.MessageOutputModel
import im.api.model.output.messages.MessageUpdateOutputModel
import im.api.model.output.messages.MessagesPaginatedOutputModel
import im.domain.Failure
import im.domain.Success
import im.domain.user.AuthenticatedUser
import im.services.messages.MessageService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@Authenticated
@RequestMapping("/api/channels/{channelId}/messages")
class MessagesController(
    private val messageService: MessageService,
    private val errorHandler: ErrorHandler,
) {
    private val handleMessagesFailure = errorHandler::handleMessagesFailure

    /**
     * Get messages for a channel.
     *
     *
     * Possible status codes:
     * - 200 OK: Messages successfully retrieved.
     * - 404 Not Found: Channel not found.
     * - 403 Forbidden: User not a member of the channel.
     *
     * @param paginationInput The pagination input.
     * @param channelId The channel identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see PaginationInputModel
     * @see ChannelIdentifierInputModel
     * @see MessagesPaginatedOutputModel
     *
     */
    @GetMapping
    fun getChannelMessages(
        @Valid paginationInput: PaginationInputModel,
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid sort: SortInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                messageService.getChannelMessages(
                    channelId.toDomain(),
                    paginationInput.toRequest(),
                    sort.toRequest(),
                    user.user,
                )
        ) {
            is Success -> ResponseEntity.ok(MessagesPaginatedOutputModel.fromMessages(res.value.items, res.value.info))
            is Failure -> handleMessagesFailure(res.value)
        }

    /**
     * Get a message by its ID.
     *
     * Possible status codes:
     * - 200 OK: Message successfully retrieved.
     * - 404 Not Found: Message not found.
     * - 403 Forbidden: User not a member of the channel.
     *
     * @param channelId The channel identifier.
     * @param messageId The message identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see MessageIdentifierInputModel
     * @see MessageOutputModel
     *
     */
    @GetMapping("/{messageId}")
    fun getMessage(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid messageId: MessageIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res = messageService.getMessageById(channelId.toDomain(), messageId.toDomain(), user.user)
        ) {
            is Success -> ResponseEntity.ok(MessageOutputModel.fromDomain(res.value))
            is Failure -> handleMessagesFailure(res.value)
        }

    /**
     * Create a new message.
     *
     * Possible status codes:
     * - 201 Created: Message successfully created.
     * - 404 Not Found: Channel not found.
     * - 403 Forbidden: User cannot send messages in the channel.
     *
     * @param channelId The channel identifier.
     * @param messageInput The message creation data.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see MessageCreationInputModel
     * @see MessageCreationOutputModel
     *
     */
    @PostMapping
    fun createMessage(
        @Valid channelId: ChannelIdentifierInputModel,
        @RequestBody @Valid messageInput: MessageCreationInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = messageService.createMessage(channelId.toDomain(), messageInput.content, user.user)) {
            is Failure -> handleMessagesFailure(res.value)
            is Success ->
                ResponseEntity
                    .created(URI("/api/channels/${channelId.channelId}/messages/${res.value.id.value}"))
                    .body(MessageCreationOutputModel.fromDomain(res.value))
        }

    /**
     * Update a message.
     *
     * Possible status codes:
     * - 200 OK: Message successfully updated.
     * - 404 Not Found: Message not found.
     * - 403 Forbidden: User cannot update the message.
     *
     * @param channelId The channel identifier.
     * @param messageId The message identifier.
     * @param messageInput The message update data.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see MessageIdentifierInputModel
     * @see MessageCreationInputModel
     * @see MessageUpdateOutputModel
     *
     */
    @PutMapping("/{messageId}")
    fun updateMessage(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid messageId: MessageIdentifierInputModel,
        @RequestBody @Valid messageInput: MessageCreationInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                messageService.updateMessage(
                    channelId.toDomain(),
                    messageId.toDomain(),
                    messageInput.content,
                    user.user,
                )
        ) {
            is Failure -> handleMessagesFailure(res.value)
            is Success -> ResponseEntity.ok(MessageUpdateOutputModel(res.value))
        }

    /**
     * Delete a message.
     *
     * Possible status codes:
     * - 204 No Content: Message successfully deleted.
     * - 404 Not Found: Message not found.
     * - 403 Forbidden: User cannot delete the message.
     *
     * @param channelId The channel identifier.
     * @param messageId The message identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see MessageIdentifierInputModel
     *
     */
    @DeleteMapping("/{messageId}")
    fun deleteMessage(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid messageId: MessageIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = messageService.deleteMessage(channelId.toDomain(), messageId.toDomain(), user.user)) {
            is Failure -> handleMessagesFailure(res.value)
            is Success -> ResponseEntity.noContent().build()
        }
}
