package user

data class User(
    val id: Long = 0,
    val name: String,
    val password: String,
    val email: String
)