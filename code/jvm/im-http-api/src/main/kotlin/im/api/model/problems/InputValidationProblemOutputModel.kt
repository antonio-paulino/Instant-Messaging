package im.api.model.problems

data class InputValidationProblemOutputModel(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    val errors: List<String>,
)
