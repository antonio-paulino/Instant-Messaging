package im.api.model.input.validators

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [PasswordValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Password(
    val message: String = "Invalid password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PasswordValidator : ConstraintValidator<Password, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        val password = value ?: return false
        var failed = false

        if (password.length < 8) {
            context.addConstraintViolation("Password must be at least 8 characters long")
            failed = true
        }

        if (password.length > 30) {
            context.addConstraintViolation("Password must be at most 30 characters long")
            failed = true
        }

        if (password.any { it.isWhitespace() }) {
            context.addConstraintViolation("Password must not contain whitespace")
            failed = true
        }

        if (!password.any { it.isDigit() }) {
            context.addConstraintViolation("Password must contain at least one digit")
            failed = true
        }

        if (!password.any { it.isLowerCase() }) {
            context.addConstraintViolation("Password must contain at least one lowercase letter")
            failed = true
        }

        if (!password.any { it.isUpperCase() }) {
            context.addConstraintViolation("Password must contain at least one uppercase letter")
            failed = true
        }

        return !failed
    }

    private fun ConstraintValidatorContext?.addConstraintViolation(message: String) =
        this?.buildConstraintViolationWithTemplate(message)?.addConstraintViolation()
}
