package im.repository.mem.model.user

import im.domain.user.User

data class UserDTO(
    val id: Long = 0,
    val name: String = "",
    val password: String = "",
    val email: String = "",
) {
    companion object {
        fun fromDomain(user: User): UserDTO =
            UserDTO(
                id = user.id.value,
                name = user.name.value,
                password = user.password.value,
                email = user.email.value,
            )
    }

    fun toDomain(): User =
        User(
            id = id,
            name = name,
            password = password,
            email = email,
        )
}
