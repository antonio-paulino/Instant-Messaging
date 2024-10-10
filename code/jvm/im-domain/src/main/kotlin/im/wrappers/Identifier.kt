package im.wrappers

/**
 * Identifier wrapper class that enforces identifier validation rules.
 *
 * An identifier must be positive.
 */
data class Identifier(
    val value: Long
) {
    init {
        require(value >= 0) { "Identifier value must be positive" }
    }

    override fun toString(): String = value.toString()
}

fun Long.toIdentifier(): Identifier = Identifier(this)
fun Int.toIdentifier(): Identifier = Identifier(this.toLong())
