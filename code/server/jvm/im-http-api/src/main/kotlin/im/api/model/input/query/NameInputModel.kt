package im.api.model.input.query

import jakarta.validation.constraints.NotBlank

/**
 * Input model for partial name search.
 *
 * @property name The name to search for.
 */
data class NameInputModel(
    @NotBlank(message = "Name cannot be blank")
    val name: String?,
)
