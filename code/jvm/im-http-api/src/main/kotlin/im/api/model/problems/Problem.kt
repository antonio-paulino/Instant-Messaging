package im.api.model.problems

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

sealed class Problem(
    typeURI: URI,
) {
    private val type = typeURI.toString()
    private val title: String = typeURI.path.split("/").last()

    fun response(
        status: HttpStatus,
        detail: String,
    ): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(
                ProblemOutputModel(
                    type,
                    title,
                    status.value(),
                    detail,
                ),
            )

    data object InvalidInvitationProblem :
        Problem(URI.create("${PROBLEMS_URI}/invalid-invitation"))

    data object UnauthorizedProblem :
        Problem(URI.create("${PROBLEMS_URI}/unauthorized"))

    data object UserAlreadyExistsProblem :
        Problem(URI.create("${PROBLEMS_URI}/user-already-exists"))

    data object CannotAccessChannelProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-access-channel"))

    data object ChannelNotFoundProblem :
        Problem(URI.create("${PROBLEMS_URI}/channel-not-found"))

    data object MessageNotFoundProblem :
        Problem(URI.create("${PROBLEMS_URI}/message-not-found"))

    data object UserNotFoundProblem :
        Problem(URI.create("${PROBLEMS_URI}/user-not-found"))

    data object CannotSendMessageProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-send-message"))

    data object CannotDeleteMessageProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-delete-message"))

    data object CannotUpdateMessageProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-update-message"))

    data object ChannelAlreadyExistsProblem :
        Problem(URI.create("${PROBLEMS_URI}/channel-already-exists"))

    data object CannotAddMemberProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-add-member"))

    data object CannotRemoveMemberProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-remove-member"))

    data object CannotDeleteChannelProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-delete-channel"))

    data object CannotUpdateChannelProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-update-channel"))

    data object CannotJoinPrivateChannelProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-join-private-channel"))

    data object UserAlreadyMemberProblem :
        Problem(URI.create("${PROBLEMS_URI}/user-already-member"))

    data object CannotDeleteInvitationProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-delete-invitation"))

    data object CannotAccessInvitationProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-access-invitation"))

    data object CannotUpdateInvitationProblem :
        Problem(URI.create("${PROBLEMS_URI}/cannot-update-invitation"))

    data object InvitationAlreadyExistsProblem :
        Problem(URI.create("${PROBLEMS_URI}/invitation-already-exists"))

    data object UserCannotInviteToChannelProblem :
        Problem(URI.create("${PROBLEMS_URI}/user-cannot-invite-to-channel"))

    data object InviteeAlreadyMemberProblem :
        Problem(URI.create("${PROBLEMS_URI}/invitee-already-member"))

    data object InvitationNotFoundProblem :
        Problem(URI.create("${PROBLEMS_URI}/invitation-not-found"))

    data object InvalidSortProblem :
        Problem(URI.create("${PROBLEMS_URI}/invalid-sort"))
}
