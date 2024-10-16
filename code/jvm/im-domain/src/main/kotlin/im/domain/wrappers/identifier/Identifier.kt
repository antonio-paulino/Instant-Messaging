package im.domain.wrappers.identifier

import im.domain.Failure
import im.domain.Success

/**
 * Identifier wrapper class that enforces identifier validation rules.
 *
 * An identifier must be positive.
 */
data class Identifier(
    val value: Long,
) {
    companion object {
        private val validator = IdentifierValidator()
    }

    init {
        val validation = validator.validate(value)
        require(validation is Success) { (validation as Failure).value.toErrorMessage() }
    }

    override fun toString(): String = value.toString()
}

fun Long.toIdentifier(): Identifier = Identifier(this)

fun Int.toIdentifier(): Identifier = Identifier(this.toLong())
