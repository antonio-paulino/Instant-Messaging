package im.domain.wrappers.name

sealed class NameValidationError(
    private val defaultMessage: String = "Name is invalid",
) {
    data object Blank : NameValidationError("Name cannot be blank")

    data class InvalidLength(
        val min: Int,
        val max: Int,
    ) : NameValidationError("Name must be between $min and $max characters")

    fun toErrorMessage(): String = defaultMessage
}

fun List<NameValidationError>.toErrorMessage(): String = joinToString("\n") { it.toErrorMessage() }
