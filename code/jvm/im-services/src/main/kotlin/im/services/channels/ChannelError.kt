package im.services.channels

sealed class ChannelError {
    data object ChannelNotFound : ChannelError()

    data class ChannelAlreadyExists(
        val conflict: String,
    ) : ChannelError()

    data object UserCannotUpdateChannel : ChannelError()

    data object UserCannotDeleteChannel : ChannelError()

    data object UserCannotAccessChannel : ChannelError()

    data object UserCannotAddMember : ChannelError()

    data object UserCannotRemoveMember : ChannelError()

    data object UserNotFound : ChannelError()

    data object CannotJoinPrivateChannel : ChannelError()

    data object UserAlreadyMember : ChannelError()

    data object UserNotMember : ChannelError()

    data class InvalidSortField(
        val field: String,
        val validFields: List<String>,
    ) : ChannelError()

    data object CannotAccessUserChannels : ChannelError()
}
