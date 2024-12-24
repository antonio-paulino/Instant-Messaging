package im.api.model.input.validators.domain

import im.repository.pagination.Sort
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates if a field is a valid [Sort].
 */
@Constraint(validatedBy = [SortValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IsSort(
    val message: String = "Invalid sort",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Sort validator.
 *
 * @see Sort
 */
class SortValidator : ConstraintValidator<IsSort, String> {
    override fun isValid(
        value: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        context?.disableDefaultConstraintViolation()

        if (!Sort.entries.any { it.name == value.uppercase() }) {
            context
                ?.buildConstraintViolationWithTemplate(
                    "Sort must be one of ${Sort.entries.joinToString { it.name }}",
                )?.addConstraintViolation()
            return false
        }

        return true
    }
}
