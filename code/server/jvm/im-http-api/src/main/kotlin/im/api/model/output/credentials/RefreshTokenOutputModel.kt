package im.api.model.output.credentials

import im.domain.tokens.RefreshToken
import java.time.LocalDateTime
import java.util.UUID

data class RefreshTokenOutputModel(
    val token: UUID,
    val expiresAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(refreshToken: RefreshToken): RefreshTokenOutputModel =
            RefreshTokenOutputModel(
                token = refreshToken.token,
                expiresAt = refreshToken.session.expiresAt,
            )
    }
}
