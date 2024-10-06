package im.model.output

import im.tokens.RefreshToken
import java.time.LocalDateTime
import java.util.*


data class RefreshTokenOutputModel(
    val token: UUID,
    val expiresAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(refreshToken: RefreshToken): RefreshTokenOutputModel {
            return RefreshTokenOutputModel(
                token = refreshToken.token,
                expiresAt = refreshToken.session.expiresAt
            )
        }
    }
}
