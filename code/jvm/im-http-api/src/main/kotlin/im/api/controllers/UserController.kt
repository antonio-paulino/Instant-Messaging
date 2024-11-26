package im.api.controllers

import im.api.middlewares.authentication.Authenticated
import im.api.middlewares.ratelimit.RateLimit
import im.api.model.input.path.UserIdentifierInputModel
import im.api.model.input.query.AfterIdInputModel
import im.api.model.input.query.NameInputModel
import im.api.model.input.query.PaginationInputModel
import im.api.model.input.query.SortInputModel
import im.api.model.input.query.UserChannelsFilterInputModel
import im.api.model.output.channel.ChannelsPaginatedOutputModel
import im.api.model.output.users.UserChannelsOutputModel
import im.api.model.output.users.UserOutputModel
import im.api.model.output.users.UsersPaginatedOutputModel
import im.domain.Failure
import im.domain.Success
import im.domain.user.AuthenticatedUser
import im.services.channels.ChannelService
import im.services.users.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@RateLimit(limitSeconds = 10)
@Authenticated
class UserController(
    private val userService: UserService,
    private val channelService: ChannelService,
    private val errorHandler: ErrorHandler,
) {
    private val handleUserFailure = errorHandler::handleUserFailure
    private val handleChannelFailure = errorHandler::handleChannelFailure

    /**
     * Get a user by their ID.
     *
     * Possible status codes:
     * - 200 OK: User successfully retrieved.
     * - 404 Not Found: User not found.
     *
     * @param userId The user identifier.
     *
     * @return The response entity.
     *
     * @see UserIdentifierInputModel
     * @see UserOutputModel
     *
     */
    @GetMapping("/{userId}")
    fun getUserById(
        @Valid userId: UserIdentifierInputModel,
    ): ResponseEntity<Any> =
        when (val result = userService.getUserById(userId.toDomain())) {
            is Success -> ResponseEntity.ok(UserOutputModel.fromDomain(result.value))
            is Failure -> handleUserFailure(result.value)
        }

    /**
     * Get a list of users.
     *
     * Possible status codes:
     * - 200 OK: Users successfully retrieved.
     * - 500 Internal Server Error: An error occurred while retrieving users.
     *
     * @param paginationInput The pagination input.
     * @param partialName The partial name input.
     *
     * @return The response entity.
     *
     * @see PaginationInputModel
     * @see NameInputModel
     * @see UsersPaginatedOutputModel
     *
     */
    @GetMapping
    fun getUsers(
        @Valid paginationInput: PaginationInputModel,
        @Valid partialName: NameInputModel?,
        @Valid sort: SortInputModel,
    ): ResponseEntity<Any> =
        when (val result = userService.getUsers(partialName?.name, paginationInput.toRequest(), sort.toRequest())) {
            is Success -> ResponseEntity.ok(UsersPaginatedOutputModel.fromDomain(result.value))
            is Failure -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }

    /**
     * Get the channels of a user.
     *
     * Possible status codes:
     * - 200 OK: Channels successfully retrieved.
     * - 404 Not Found: User not found.
     * - 403 Forbidden: User cannot access the channels.
     *
     * @param userId The user identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see UserIdentifierInputModel
     * @see UserChannelsOutputModel
     *
     */
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/{userId}/channels")
    fun getUserChannels(
        @Valid userId: UserIdentifierInputModel,
        @Valid sort: SortInputModel,
        @Valid pagination: PaginationInputModel,
        @Valid filter: UserChannelsFilterInputModel,
        @Valid after: AfterIdInputModel?,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val result =
                channelService.getUserChannels(
                    userId.toDomain(),
                    sort.toRequest(),
                    pagination.toRequest(),
                    filter.filterOwned.toBoolean(),
                    after?.after?.toDomain(),
                    user.user,
                )
        ) {
            is Success -> ResponseEntity.ok(ChannelsPaginatedOutputModel.fromDomain(result.value))
            is Failure -> handleChannelFailure(result.value)
        }
}
