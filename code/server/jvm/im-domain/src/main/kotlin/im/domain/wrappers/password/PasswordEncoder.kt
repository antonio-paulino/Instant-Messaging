package im.domain.wrappers.password

/**
 * Interface for password encoders.
 */
interface PasswordEncoder {
    /**
     * Encodes a password.
     *
     * @param password the password
     * @param iterations the number of iterations to hash the password
     * @return the encoded password
     */
    fun encode(
        password: Password,
        iterations: Int = 1000,
    ): Password

    /**
     * Verifies a password against a stored password.
     *
     * @param password the password
     * @param storedPassword the stored password
     * @param iterations the number of iterations to hash the password
     * @return `true` if the password is correct, `false` otherwise
     */
    fun verify(
        password: Password,
        storedPassword: Password,
        iterations: Int = 1000,
    ): Boolean
}
