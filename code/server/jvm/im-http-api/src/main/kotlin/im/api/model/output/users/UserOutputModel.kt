package im.api.model.output.users

import im.domain.user.User

data class UserOutputModel(
    val id: Long,
    val name: String,
    val email: String,
) {
    companion object {
        fun fromDomain(user: User): UserOutputModel =
            UserOutputModel(
                id = user.id.value,
                name = user.name.value,
                email = user.email.value,
            )
    }
}
