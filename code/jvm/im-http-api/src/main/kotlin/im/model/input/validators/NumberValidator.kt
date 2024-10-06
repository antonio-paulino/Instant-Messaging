package im.model.input.validators

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [NumberValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IsNumber(
    val message: String = "Field must be a number",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NumberValidator : ConstraintValidator<IsNumber, String> {
    override fun isValid(value: String?, context: jakarta.validation.ConstraintValidatorContext?): Boolean {
        return value?.toIntOrNull() != null
    }
}