package im.model.input.validators

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Constraint(validatedBy = [AtLeastOneNotNullValidator::class])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class OneNotNull(
    val fields: Array<String> = [],
    val message: String = "At least one of email or username must be provided",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class AtLeastOneNotNullValidator : ConstraintValidator<OneNotNull, Any> {
    private lateinit var fields: Array<String>

    override fun initialize(constraintAnnotation: OneNotNull) {
        fields = constraintAnnotation.fields
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        val props = value?.javaClass?.kotlin?.memberProperties ?: return false
        val oneNotNullFields = props.filter { it.name in fields }
        return oneNotNullFields.any { it.get(value) != null }
    }
}