package im.services.messages

sealed class MessageError {
    data object ChannelNotFound : MessageError()

    data object UserNotInChannel : MessageError()

    data object NoWritePermission : MessageError()

    data object MessageNotFound : MessageError()

    data object CannotEditMessage : MessageError()

    data object CannotDeleteMessage : MessageError()

    data class InvalidSortField(
        val field: String,
        val validFields: List<String>,
    ) : MessageError()
}
