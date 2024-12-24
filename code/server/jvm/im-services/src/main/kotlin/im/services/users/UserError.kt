package im.services.users

sealed class UserError {
    data object UserNotFound : UserError()

    data class InvalidSortField(
        val field: String,
        val validFields: List<String>,
    ) : UserError()
}
