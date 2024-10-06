package im.model.output

import im.user.User

data class UserOutputModel(
    val id: Long,
    val name: String,
    val email: String
) {
    companion object {
        fun fromDomain(user: User): UserOutputModel {
            return UserOutputModel(
                id = user.id,
                name = user.name,
                email = user.email
            )
        }
    }
}
