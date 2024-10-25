package im.services.channels

sealed class ChannelError {
    data object ChannelNotFound : ChannelError()

    data class ChannelAlreadyExists(
        val conflict: String,
    ) : ChannelError()

    data object CannotUpdateChannel : ChannelError()

    data object CannotDeleteChannel : ChannelError()

    data object CannotAccessChannel : ChannelError()

    data object CannotAddMember : ChannelError()

    data object CannotRemoveMember : ChannelError()

    data object UserNotFound : ChannelError()

    data object CannotJoinPrivateChannel : ChannelError()

    data object UserAlreadyMember : ChannelError()

    data object UserNotMember : ChannelError()

    data object CannotUpdateMemberRole : ChannelError()

    data object InvalidDefaultRole : ChannelError()

    data class InvalidSortField(
        val field: String,
        val validFields: List<String>,
    ) : ChannelError()

    data object CannotAccessUserChannels : ChannelError()
}
