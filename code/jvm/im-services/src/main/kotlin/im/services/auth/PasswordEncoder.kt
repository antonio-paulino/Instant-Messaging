package im.services.auth

import jakarta.inject.Named
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Named
class PasswordEncoder {

    fun encode(password: String, iterations: Int = 1000): String {
        val salt = generateSalt()
        val hash = hashPassword(password, salt, iterations)
        return "${Base64.getEncoder().encodeToString(salt)}:$hash"
    }

    fun verify(password: String, storedPassword: String, iterations: Int = 1000): Boolean {
        val parts = storedPassword.split(":")
        val salt = Base64.getDecoder().decode(parts[0])
        val hash = parts[1]
        val passwordHash = hashPassword(password, salt, iterations)
        return passwordHash == hash
    }

    private fun hashPassword(password: String, salt: ByteArray, iterations: Int): String {
        var hash = password.toByteArray() + salt
        val digest = MessageDigest.getInstance("SHA-256")
        for (i in 1..iterations) {
            hash = digest.digest(hash)
        }
        return Base64.getEncoder().encodeToString(hash)
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(salt)
        return salt
    }
}
