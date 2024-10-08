package im.user

data class User(
    val id: Long = 0,
    val name: String,
    val password: String,
    val email: String
) {
    init {
        require(id >= 0) { "User ID must be positive" }
        require(name.isNotBlank()) { "User name cannot be blank" }
        require(name.length in 3..30) { "User name must be between 3 and 30 characters" }
        require(password.isNotBlank()) { "User password cannot be blank" }
        require(password.length in 8..80) { "User password must be between 8 and 80 characters" }
        require(email.isNotBlank()) { "User email cannot be blank" }
        require(email.length in 5..50) { "User email must be between 5 and 50 characters" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"))) { "User email must be a valid email address" }
    }
}