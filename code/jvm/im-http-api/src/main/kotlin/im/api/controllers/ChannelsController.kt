package im.api.controllers

import im.api.middlewares.authentication.Authenticated
import im.api.middlewares.ratelimit.RateLimit
import im.api.model.input.body.ChannelCreationInputModel
import im.api.model.input.body.ChannelRoleUpdateInputModel
import im.api.model.input.path.ChannelIdentifierInputModel
import im.api.model.input.path.UserIdentifierInputModel
import im.api.model.input.query.NameInputModel
import im.api.model.input.query.PaginationInputModel
import im.api.model.input.query.SortInputModel
import im.api.model.output.channel.ChannelCreationOutputModel
import im.api.model.output.channel.ChannelOutputModel
import im.api.model.output.channel.ChannelsPaginatedOutputModel
import im.domain.Failure
import im.domain.Success
import im.domain.user.AuthenticatedUser
import im.services.channels.ChannelService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@Authenticated
@RateLimit(limitSeconds = 10)
@RequestMapping("/api/channels")
class ChannelsController(
    private val channelService: ChannelService,
    private val errorHandler: ErrorHandler,
) {
    private val handleChannelFailure = errorHandler::handleChannelFailure

    /**
     * Create a new channel.
     *
     *
     * Possible status codes:
     *  - 201 Created: Channel successfully created.
     *  - 400 Bad Request: Invalid input data.
     *  - 409 Conflict: Channel already exists.
     * @param channelInput The channel creation data.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelCreationInputModel
     * @see ChannelCreationOutputModel
     *
     */
    @PostMapping
    fun createChannel(
        @RequestBody @Valid channelInput: ChannelCreationInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                channelService.createChannel(
                    channelInput.name.toDomain(),
                    channelInput.defaultRole.toDomain(),
                    channelInput.isPublic.toBoolean(),
                    user.user,
                )
        ) {
            is Success ->
                ResponseEntity
                    .created(URI("/api/channels/${res.value.id}"))
                    .body(ChannelCreationOutputModel.fromDomain(res.value))

            is Failure -> handleChannelFailure(res.value)
        }

    /**
     * Get a channel by its ID.
     *
     * Possible status codes:
     * - 200 OK: Channel successfully retrieved.
     * - 404 Not Found: Channel not found.
     * - 403 Forbidden: User cannot access the channel.
     *
     * @param channelId The channel identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see ChannelOutputModel
     *
     */
    @GetMapping("/{channelId}")
    fun getChannelById(
        @Valid channelId: ChannelIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = channelService.getChannelById(channelId.toDomain(), user.user)) {
            is Success -> ResponseEntity.ok(ChannelOutputModel.fromDomain(res.value))
            is Failure -> handleChannelFailure(res.value)
        }

    /**
     * Get a list of channels.
     *
     * Possible status codes:
     * - 200 OK: Channels successfully retrieved.
     *
     * @param pagination The pagination input.
     * @param partialName The partial name input.
     *
     * @return The response entity.
     *
     * @see PaginationInputModel
     * @see NameInputModel
     * @see ChannelsPaginatedOutputModel
     */
    @GetMapping
    fun getChannels(
        @Valid pagination: PaginationInputModel,
        @Valid sort: SortInputModel,
        @Valid partialName: NameInputModel?,
    ): ResponseEntity<Any> =
        when (val res = channelService.getChannels(partialName?.name, pagination.toRequest(), sort.toRequest())) {
            is Success -> ResponseEntity.ok(ChannelsPaginatedOutputModel.fromDomain(res.value))
            is Failure -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }

    /**
     * Update a channel.
     *
     *
     * Possible status codes:
     * - 204 No Content: Channel successfully updated.
     * - 404 Not Found: Channel not found.
     * - 403 Forbidden: User cannot update the channel.
     * @param channelId The channel identifier.
     * @param channelInput The channel update data.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see ChannelCreationInputModel
     */
    @PutMapping("/{channelId}")
    fun updateChannel(
        @Valid channelId: ChannelIdentifierInputModel,
        @RequestBody @Valid channelInput: ChannelCreationInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                channelService.updateChannel(
                    channelId.toDomain(),
                    channelInput.name.toDomain(),
                    channelInput.defaultRole.toDomain(),
                    channelInput.isPublic.toBoolean(),
                    user.user,
                )
        ) {
            is Success -> ResponseEntity.noContent().build()
            is Failure -> handleChannelFailure(res.value)
        }

    /**
     * Delete a channel.
     *
     * Possible status codes:
     * - 204 No Content: Channel successfully deleted.
     * - 404 Not Found: Channel not found.
     * - 403 Forbidden: User cannot delete the channel.
     * @param channelId The channel identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     */
    @DeleteMapping("/{channelId}")
    fun deleteChannel(
        @Valid channelId: ChannelIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = channelService.deleteChannel(channelId.toDomain(), user.user)) {
            is Success -> {
                ResponseEntity.noContent().build()
            }
            is Failure -> handleChannelFailure(res.value)
        }

    /**
     * Add a user to a channel.
     *
     * Possible status codes:
     * - 201 Created: User successfully added to the channel.
     * - 404 Not Found: Channel or user not found.
     * - 403 Forbidden: User cannot add members to the channel.
     *
     * @param channelId The channel identifier.
     * @param userId The user identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see UserIdentifierInputModel
     *
     */
    @PutMapping("/{channelId}/members/{userId}")
    fun joinChannel(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid userId: UserIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = channelService.joinChannel(channelId.toDomain(), userId.toDomain(), user.user)) {
            is Success -> {
                ResponseEntity.created(URI("/api/channels/${channelId.channelId.value}/members/${userId.userId.value}"))
                    .build()
            }
            is Failure -> handleChannelFailure(res.value)
        }

    /**
     * Remove a user from a channel.
     *
     * Possible status codes:
     * - 204 No Content: User successfully removed from the channel.
     * - 404 Not Found: Channel or user not found.
     * - 403 Forbidden: User cannot remove members from the channel.
     *
     * @param channelId The channel identifier.
     * @param userId The user identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see UserIdentifierInputModel
     *
     */
    @DeleteMapping("/{channelId}/members/{userId}")
    fun removeChannelUser(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid userId: UserIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = channelService.removeChannelMember(channelId.toDomain(), userId.toDomain(), user.user)) {
            is Success -> {
                ResponseEntity.noContent().build()
            }
            is Failure -> handleChannelFailure(res.value)
        }

    @PatchMapping("/{channelId}/members/{userId}")
    fun updateMemberRole(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid userId: UserIdentifierInputModel,
        @RequestBody @Valid role: ChannelRoleUpdateInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = channelService.updateMemberRole(channelId.toDomain(), userId.toDomain(), role.toDomain(), user.user)) {
            is Success -> ResponseEntity.noContent().build()
            is Failure -> handleChannelFailure(res.value)
        }
}
