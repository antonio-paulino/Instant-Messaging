package im.domain.messages

sealed class MessageValidationError(
    val defaultMessage: String = "Invalid message",
) {
    data object ContentBlank : MessageValidationError("Message content cannot be blank")

    data class ContentLength(
        val min: Int,
        val max: Int,
    ) : MessageValidationError("Message content must be between $min and $max characters")

    fun toErrorMessage(): String = defaultMessage
}

fun List<MessageValidationError>.toErrorMessage(): String = joinToString("\n") { it.toString() }
