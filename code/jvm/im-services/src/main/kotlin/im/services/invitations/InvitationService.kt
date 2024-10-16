package im.services.invitations

import im.domain.Either
import im.domain.channel.ChannelRole
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.invitations.ImInvitation
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.services.auth.AuthError
import java.time.LocalDateTime

interface InvitationService {
    /**
     * Creates an invitation for a user to join a channel
     *
     * - The inviter must be the owner of the channel to create an invitation
     * - The invitee must not already be a member of the channel
     * - The invitee must not already have a pending invitation to the channel
     * - The expiration date must be at least 15 minutes in the future
     * - The expiration date must be at most 30 days in the future
     *
     *
     * @param channelId the ID of the channel to invite the user to
     * @param inviteeId the ID of the user to invite
     * @param expirationDate the date when the invitation will expire
     * @param role the role the user will have in the channel
     * @param inviter the user who is inviting the invitee
     *
     * @return the created invitation or an error if the invitation could not be created
     */
    fun createChannelInvitation(
        channelId: Identifier,
        inviteeId: Identifier,
        expirationDate: LocalDateTime,
        role: ChannelRole,
        inviter: User,
    ): Either<InvitationError, ChannelInvitation>

    /**
     * Creates an Instant Messaging invitation.
     *
     *
     * @param expiration the expiration date of the invitation
     * @return the invitation if it is created, or an [AuthError] otherwise
     */
    fun createImInvitation(expiration: LocalDateTime?): Either<InvitationError, ImInvitation>

    /**
     * Gets an invitation's details
     *
     * - The user must be the inviter or the invitee to view the invitation
     *
     * @param channelId the ID of the channel to invite the user to
     * @param inviteId the ID of the invitation to get
     * @param user the user requesting the invitation
     *
     * @return the invitation or an error if the user does not have permission to view the invitation
     */
    fun getInvitation(
        channelId: Identifier,
        inviteId: Identifier,
        user: User,
    ): Either<InvitationError, ChannelInvitation>

    /**
     * Gets invitations for a channel
     *
     * - The user must be the owner of the channel to view the invitations for the channel
     *
     * @param channelId the ID of the channel to get the invitations for
     * @param user the user requesting the invitations
     * @param sortRequest the sort request
     * @param paginationRequest the pagination request
     *
     * @return a list of invitations for the channel or an error if the user does not have permission to view the
     * invitations
     */
    fun getChannelInvitations(
        channelId: Identifier,
        user: User,
        sortRequest: SortRequest,
        paginationRequest: PaginationRequest,
    ): Either<InvitationError, Pagination<ChannelInvitation>>

    /**
     * Updates an invitation for a user to join a channel
     *
     * - The user must be the inviter to update the invitation
     * - The new expiration date must be at least 15 minutes in the future
     * - The new expiration date must be at most 30 days in the future
     *
     * @param channelId the ID of the channel to invite the user to
     * @param inviteId the ID of the invitation to update
     * @param role the role the user will have in the channel
     * @param expirationDate the date when the invitation will expire
     * @param user the user updating the invitation
     *
     * @return an error if the invitation could not be updated
     */
    fun updateInvitation(
        channelId: Identifier,
        inviteId: Identifier,
        role: ChannelRole,
        expirationDate: LocalDateTime,
        user: User,
    ): Either<InvitationError, Unit>

    /**
     * Deletes an invitation for a user to join a channel
     *
     * - The user must be the inviter to delete the invitation
     *
     * @param channelId the ID of the channel to invite the user to
     * @param inviteId the ID of the invitation to delete
     * @param user the user deleting the invitation
     *
     * @return an error if the invitation could not be deleted
     */
    fun deleteInvitation(
        channelId: Identifier,
        inviteId: Identifier,
        user: User,
    ): Either<InvitationError, Unit>

    /**
     * Gets the invitations a user has received
     *
     * - A user can only view the invitations they have received
     *
     * @param userId the ID of the user to get the invitations for
     * @param user the user requesting the invitations
     * @param sortRequest the sort request
     * @param paginationRequest the pagination request
     *
     * @return a list of invitations for the user to accept or an error if the user does not have permission to view the
     * invitations
     */
    fun getUserInvitations(
        userId: Identifier,
        user: User,
        sortRequest: SortRequest,
        paginationRequest: PaginationRequest,
    ): Either<InvitationError, Pagination<ChannelInvitation>>

    /**
     * Accepts or rejects an invitation for a user to join a channel
     *
     * - The user must be the invitee to accept or reject the invitation
     *
     * @param userId the ID of the user to accept or reject the invitation for
     * @param invitationIdentifier the ID of the invitation to accept or reject
     * @param status whether to accept or reject the invitation
     * @param user the user accepting or rejecting the invitation
     *
     * @return an error if the invitation could not be accepted or rejected
     */
    fun acceptOrRejectInvitation(
        userId: Identifier,
        invitationIdentifier: Identifier,
        status: ChannelInvitationStatus,
        user: User,
    ): Either<InvitationError, Unit>
}
