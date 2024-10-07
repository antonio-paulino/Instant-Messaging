package im.model.input.validators

import im.pagination.Sort
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [SortValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IsSort(
    val message: String = "Sort must be one of: ASC, DESC",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class SortValidator : ConstraintValidator<IsSort, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.let { Sort.entries.any { it.name == value.uppercase() } } ?: false
    }
}