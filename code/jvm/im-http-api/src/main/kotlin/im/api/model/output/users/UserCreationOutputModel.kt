package im.api.model.output.users

import im.domain.user.User

class UserCreationOutputModel(
    val id: Long,
) {
    companion object {
        fun fromDomain(user: User): UserCreationOutputModel =
            UserCreationOutputModel(
                id = user.id.value,
            )
    }
}
