package im.api.model.output.credentials

import im.domain.tokens.AccessToken
import java.time.LocalDateTime
import java.util.UUID

data class AccessTokenOutputModel(
    val token: UUID,
    val expiresAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(accessToken: AccessToken): AccessTokenOutputModel =
            AccessTokenOutputModel(
                token = accessToken.token,
                expiresAt = accessToken.expiresAt,
            )
    }
}
