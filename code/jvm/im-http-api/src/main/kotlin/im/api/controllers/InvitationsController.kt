package im.api.controllers

import im.api.model.input.AuthenticatedUser
import im.api.model.input.body.ChannelInvitationCreationInputModel
import im.api.model.input.body.ChannelInvitationUpdateInputModel
import im.api.model.input.body.InvitationAcceptInputModel
import im.api.model.input.path.ChannelIdentifierInputModel
import im.api.model.input.path.InvitationIdentifierInputModel
import im.api.model.input.path.UserIdentifierInputModel
import im.api.model.input.query.SortInputModel
import im.api.model.output.invitations.ChannelInvitationCreationOutputModel
import im.api.model.output.invitations.ChannelInvitationOutputModel
import im.api.model.output.invitations.ChannelInvitationsOutputModel
import im.services.Failure
import im.services.Success
import im.services.invitations.InvitationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api")
class InvitationsController(
    private val invitationService: InvitationService,
    private val errorHandler: ErrorHandler,
) {
    private val handleInvitationFailure = errorHandler::handleInvitationFailure

    /**
     * Create a new invitation.
     *
     * Possible status codes:
     *  - 201 Created: Invitation successfully created.
     *  - 400 Bad Request: Invalid input data.
     *  - 404 Not Found: Channel not found.
     *  - 403 Forbidden: User cannot create invitation.
     *
     * @param channelId The channel identifier.
     * @param invitationInput The invitation creation data.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see ChannelInvitationCreationInputModel
     * @see ChannelInvitationCreationOutputModel
     *
     */
    @PostMapping("/channels/{channelId}/invitations")
    fun createInvitation(
        @Valid channelId: ChannelIdentifierInputModel,
        @RequestBody @Valid invitationInput: ChannelInvitationCreationInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                invitationService.createInvitation(
                    channelId.toDomain(),
                    invitationInput.invitee.toDomain(),
                    invitationInput.expiresAt,
                    invitationInput.role.toDomain(),
                    user.user,
                )
        ) {
            is Success ->
                ResponseEntity
                    .created(URI("/api/channels/${channelId.channelId.value}/invitations/${res.value.id.value}"))
                    .body(ChannelInvitationCreationOutputModel.fromDomain(res.value))

            is Failure -> handleInvitationFailure(res.value)
        }

    /**
     * Get an invitation by its ID.
     *
     * Possible status codes:
     *  - 200 OK: Invitation successfully retrieved.
     *  - 404 Not Found: Invitation not found.
     *  - 403 Forbidden: User cannot access the invitation.
     *
     * @param channelId The channel identifier.
     * @param invitationId The invitation identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see InvitationIdentifierInputModel
     * @see ChannelInvitationOutputModel
     *
     */
    @GetMapping("/channels/{channelId}/invitations/{invitationId}")
    fun getInvitation(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid invitationId: InvitationIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                invitationService.getInvitation(
                    channelId.toDomain(),
                    invitationId.toDomain(),
                    user.user,
                )
        ) {
            is Success -> ResponseEntity.ok(ChannelInvitationOutputModel.fromDomain(res.value))
            is Failure -> handleInvitationFailure(res.value)
        }

    /**
     * Get a list of invitations for a channel.
     *
     * Possible status codes:
     *  - 200 OK: Invitations successfully retrieved.
     *  - 404 Not Found: Channel not found.
     *  - 403 Forbidden: User cannot access the invitations.
     *
     * @param channelId The channel identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see ChannelInvitationsOutputModel
     *
     */
    @GetMapping("/channels/{channelId}/invitations")
    fun getChannelInvitations(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid sort: SortInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = invitationService.getChannelInvitations(channelId.toDomain(), user.user, sort.toRequest())) {
            is Success -> ResponseEntity.ok(ChannelInvitationsOutputModel.fromDomain(res.value))
            is Failure -> handleInvitationFailure(res.value)
        }

    /**
     * Update an invitation.
     *
     * Possible status codes:
     *  - 204 No Content: Invitation successfully updated.
     *  - 404 Not Found: Invitation not found.
     *  - 403 Forbidden: User cannot update the invitation.
     *
     * @param channelId The channel identifier.
     * @param invitationId The invitation identifier.
     * @param invitationInput The invitation update data.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see InvitationIdentifierInputModel
     * @see ChannelInvitationUpdateInputModel
     *
     */
    @PatchMapping("/channels/{channelId}/invitations/{invitationId}")
    fun updateInvitation(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid invitationId: InvitationIdentifierInputModel,
        @RequestBody @Valid invitationInput: ChannelInvitationUpdateInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                invitationService.updateInvitation(
                    channelId.toDomain(),
                    invitationId.toDomain(),
                    invitationInput.role.toDomain(),
                    invitationInput.expiresAt,
                    user.user,
                )
        ) {
            is Success -> ResponseEntity.noContent().build()
            is Failure -> handleInvitationFailure(res.value)
        }

    /**
     * Delete an invitation.
     *
     * Possible status codes:
     *  - 204 No Content: Invitation successfully deleted.
     *  - 404 Not Found: Invitation not found.
     *  - 403 Forbidden: User cannot delete the invitation.
     *
     * @param channelId The channel identifier.
     * @param invitationId The invitation identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see ChannelIdentifierInputModel
     * @see InvitationIdentifierInputModel
     *
     */
    @DeleteMapping("/channels/{channelId}/invitations/{invitationId}")
    fun deleteInvitation(
        @Valid channelId: ChannelIdentifierInputModel,
        @Valid invitationId: InvitationIdentifierInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                invitationService.deleteInvitation(
                    channelId.toDomain(),
                    invitationId.toDomain(),
                    user.user,
                )
        ) {
            is Success -> ResponseEntity.noContent().build()
            is Failure -> handleInvitationFailure(res.value)
        }

    /**
     * Get a list of invitations for a user.
     *
     * Possible status codes:
     *  - 200 OK: Invitations successfully retrieved.
     *  - 404 Not Found: User not found.
     *  - 403 Forbidden: User cannot access the invitations.
     *
     * @param userId The user identifier.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see UserIdentifierInputModel
     * @see ChannelInvitationsOutputModel
     *
     */
    @GetMapping("/users/{userId}/invitations")
    fun getUserInvitations(
        @Valid userId: UserIdentifierInputModel,
        @Valid sort: SortInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (val res = invitationService.getUserInvitations(userId.toDomain(), user.user, sort.toRequest())) {
            is Success -> ResponseEntity.ok(ChannelInvitationsOutputModel.fromDomain(res.value))
            is Failure -> handleInvitationFailure(res.value)
        }

    /**
     * Accept or reject an invitation.
     *
     * Possible status codes:
     * - 204 No Content: Invitation successfully accepted or rejected.
     * - 404 Not Found: Invitation not found.
     * - 403 Forbidden: User cannot accept or reject the invitation.
     *
     * @param userId The user identifier.
     * @param invitationId The invitation identifier.
     * @param accept The invitation accept/reject data.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see UserIdentifierInputModel
     * @see InvitationIdentifierInputModel
     * @see InvitationAcceptInputModel
     *
     */
    @PatchMapping("/users/{userId}/invitations/{invitationId}")
    fun acceptOrRejectInvitation(
        @Valid userId: UserIdentifierInputModel,
        @Valid invitationId: InvitationIdentifierInputModel,
        @RequestBody @Valid accept: InvitationAcceptInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> =
        when (
            val res =
                invitationService.acceptOrRejectInvitation(
                    userId.toDomain(),
                    invitationId.toDomain(),
                    im.domain.invitations.ChannelInvitationStatus
                        .valueOf(accept.status.uppercase()),
                    user.user,
                )
        ) {
            is Success -> ResponseEntity.noContent().build()
            is Failure -> handleInvitationFailure(res.value)
        }
}
