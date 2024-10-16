package im.domain.wrappers.identifier

sealed class IdentifierValidationError(
    private val defaultMessage: String = "Identifier is invalid",
) {
    data object NegativeValue : IdentifierValidationError("Identifier value must be positive")

    fun toErrorMessage(): String = defaultMessage
}

fun List<IdentifierValidationError>.toErrorMessage(): String = joinToString("\n") { it.toErrorMessage() }
