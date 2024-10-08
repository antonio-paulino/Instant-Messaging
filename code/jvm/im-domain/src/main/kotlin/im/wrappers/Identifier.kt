package im.wrappers

data class Identifier(
    val value: Long
) {
    init {
        require(value >= 0) { "Identifier value must be positive" }
    }

    override fun toString(): String = value.toString()
}

fun Long.toIdentifier(): Identifier = Identifier(this)