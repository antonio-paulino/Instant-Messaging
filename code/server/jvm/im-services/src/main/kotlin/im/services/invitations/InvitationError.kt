package im.services.invitations

sealed class InvitationError {
    data object ChannelNotFound : InvitationError()

    data object InviteeNotFound : InvitationError()

    data object InviteeAlreadyMember : InvitationError()

    data object InvitationAlreadyExists : InvitationError()

    data class InvalidInvitationExpiration(
        val message: String,
    ) : InvitationError()

    data object InvitationNotFound : InvitationError()

    data object UserCannotDeleteInvitation : InvitationError()

    data object UserCannotAccessInvitation : InvitationError()

    data object UserCannotInviteToChannel : InvitationError()

    data object UserCannotUpdateInvitation : InvitationError()

    data object InvitationInvalid : InvitationError()

    data object OwnerInvitationNotAllowed : InvitationError()

    data class InvalidSortField(
        val field: String,
        val validFields: List<String>,
    ) : InvitationError()
}
