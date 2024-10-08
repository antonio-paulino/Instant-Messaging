package im.wrappers

data class Password(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Password cannot be blank" }
        require(value.length in 8..80) { "Password must be between 8 and 80 characters" }
    }

    override fun toString(): String = value
}

fun String.toPassword(): Password = Password(this)

