package im.api.controllers

import im.api.model.problems.Problem
import im.services.auth.AuthError
import im.services.channels.ChannelError
import im.services.invitations.InvitationError
import im.services.messages.MessageError
import im.services.users.UserError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class ErrorHandler {
    fun handleChannelFailure(failure: ChannelError): ResponseEntity<Any> =
        when (failure) {
            is ChannelError.ChannelAlreadyExists ->
                Problem.ChannelAlreadyExistsProblem.response(
                    HttpStatus.CONFLICT,
                    "Channel with that ${failure.conflict} already exists.",
                )

            ChannelError.ChannelNotFound ->
                Problem.ChannelNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "Channel not found",
                )

            ChannelError.UserCannotAccessChannel ->
                Problem.CannotAccessChannelProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this channel",
                )

            ChannelError.UserCannotAddMember ->
                Problem.CannotAddMemberProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You cannot add members to this channel",
                )

            ChannelError.UserCannotRemoveMember ->
                Problem.CannotRemoveMemberProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You cannot remove members from this channel",
                )

            ChannelError.UserNotFound ->
                Problem.UserNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "User not found",
                )

            ChannelError.UserCannotDeleteChannel ->
                Problem.CannotDeleteChannelProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete this channel",
                )

            ChannelError.UserCannotUpdateChannel ->
                Problem.CannotUpdateChannelProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You cannot update this channel",
                )

            ChannelError.CannotJoinPrivateChannel ->
                Problem.CannotJoinPrivateChannelProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You cannot join this channel, it is private",
                )

            ChannelError.UserAlreadyMember ->
                Problem.UserAlreadyMemberProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "User is already a member of this channel",
                )

            ChannelError.UserNotMember ->
                Problem.UserNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "User is not a member of this channel",
                )

            is ChannelError.InvalidSortField ->
                Problem.InvalidSortProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field '${failure.field}'. Valid fields are ${failure.validFields.joinToString()}",
                )

            ChannelError.CannotAccessUserChannels ->
                Problem.CannotAccessChannelProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this user's channels",
                )
        }

    fun handleInvitationFailure(failure: InvitationError): ResponseEntity<Any> =
        when (failure) {
            InvitationError.ChannelNotFound ->
                Problem.ChannelNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "Channel not found",
                )

            InvitationError.InvitationNotFound ->
                Problem.InvitationNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "Invitation not found",
                )

            InvitationError.UserCannotDeleteInvitation ->
                Problem.CannotDeleteInvitationProblem.response(
                    HttpStatus.FORBIDDEN,
                    "User cannot delete invitation",
                )

            InvitationError.UserCannotAccessInvitation ->
                Problem.CannotAccessInvitationProblem.response(
                    HttpStatus.FORBIDDEN,
                    "User cannot access invitation",
                )

            InvitationError.UserCannotUpdateInvitation ->
                Problem.CannotUpdateInvitationProblem.response(
                    HttpStatus.FORBIDDEN,
                    "User cannot update invitation",
                )

            is InvitationError.InvalidInvitationExpiration ->
                Problem.InvalidInvitationProblem.response(
                    HttpStatus.BAD_REQUEST,
                    failure.message,
                )

            InvitationError.InvitationAlreadyExists ->
                Problem.InvitationAlreadyExistsProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invitation already exists",
                )

            InvitationError.InvitationInvalid ->
                Problem.InvalidInvitationProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invitation is invalid",
                )

            InvitationError.InviteeAlreadyMember ->
                Problem.InviteeAlreadyMemberProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invitee is already a member",
                )

            InvitationError.UserCannotInviteToChannel ->
                Problem.UserCannotInviteToChannelProblem.response(
                    HttpStatus.FORBIDDEN,
                    "User cannot invite to channel",
                )

            InvitationError.InviteeNotFound ->
                Problem.UserNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "Invitee not found",
                )

            is InvitationError.InvalidSortField ->
                Problem.InvalidSortProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field '${failure.field}'. Valid fields are: ${failure.validFields.joinToString()}",
                )
        }

    fun handleMessagesFailure(error: MessageError): ResponseEntity<Any> =
        when (error) {
            is MessageError.ChannelNotFound ->
                Problem.ChannelNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "Channel not found",
                )

            MessageError.MessageNotFound ->
                Problem.MessageNotFoundProblem.response(
                    HttpStatus.NOT_FOUND,
                    "Message not found",
                )

            is MessageError.UserNotInChannel ->
                Problem.CannotAccessChannelProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You are not a member of this channel",
                )

            MessageError.NoWritePermission ->
                Problem.CannotSendMessageProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to send messages in this channel",
                )

            MessageError.CannotDeleteMessage ->
                Problem.CannotDeleteMessageProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to delete this message",
                )

            MessageError.CannotEditMessage ->
                Problem.CannotUpdateMessageProblem.response(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to edit this message",
                )

            is MessageError.InvalidSortField ->
                Problem.InvalidSortProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field: ${error.field}. Valid fields are: ${error.validFields.joinToString()}",
                )
        }

    fun handleUserFailure(failure: UserError): ResponseEntity<Any> =
        when (failure) {
            is UserError.UserNotFound -> Problem.UserNotFoundProblem.response(HttpStatus.NOT_FOUND, "User not found")

            is UserError.InvalidSortField ->
                Problem.InvalidSortProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field '${failure.field}'. Valid fields are: ${failure.validFields.joinToString()}",
                )
        }

    fun handleAuthFailure(error: AuthError): ResponseEntity<Any> =
        when (error) {
            AuthError.InvalidCredentials ->
                Problem.UnauthorizedProblem.response(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials provided",
                )

            is AuthError.UserAlreadyExists ->
                Problem.UserAlreadyExistsProblem.response(
                    HttpStatus.CONFLICT,
                    "User with that ${error.conflict} already exists.",
                )

            AuthError.InvalidToken ->
                Problem.UnauthorizedProblem.response(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token",
                )

            AuthError.SessionExpired ->
                Problem.UnauthorizedProblem.response(
                    HttpStatus.UNAUTHORIZED,
                    "Session expired",
                )

            AuthError.TokenExpired ->
                Problem.UnauthorizedProblem.response(
                    HttpStatus.UNAUTHORIZED,
                    "Access token expired",
                )

            AuthError.InvalidInvitationCode ->
                Problem.InvalidInvitationProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invalid invitation code",
                )

            AuthError.InvitationAlreadyUsed ->
                Problem.InvalidInvitationProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invitation code already used",
                )

            AuthError.InvitationExpired ->
                Problem.InvalidInvitationProblem.response(
                    HttpStatus.BAD_REQUEST,
                    "Invitation code expired",
                )

            is AuthError.InvalidInvitationExpiration ->
                Problem.InvalidInvitationProblem.response(
                    HttpStatus.BAD_REQUEST,
                    error.message,
                )
        }
}
